/*
 * Copyright (c) 2020 The Ontario Institute for Cancer Research. All rights reserved
 *
 * This program and the accompanying materials are made available under the terms of the GNU Affero General Public License v3.0.
 * You should have received a copy of the GNU Affero General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.icgc.argo.workflow.relay.service;

import static java.lang.String.format;
import static org.icgc.argo.workflow.relay.util.NextflowDocumentConverter.buildWorkflowDocument;
import static org.icgc.argo.workflow.relay.util.OffsetDateTimeDeserializer.getOffsetDateTimeModule;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.icgc.argo.workflow.relay.model.index.WorkflowDocument;
import org.icgc.argo.workflow.relay.config.elastic.ElasticsearchProperties;
import org.icgc.argo.workflow.relay.config.stream.IndexStream;
import org.icgc.argo.workflow.relay.model.management.WfManagementEvent;
import org.icgc.argo.workflow.relay.model.nextflow.TaskEvent;
import org.icgc.argo.workflow.relay.model.nextflow.WorkflowEvent;
import org.icgc.argo.workflow.relay.util.NextflowDocumentConverter;
import org.icgc.argo.workflow.relay.model.index.WorkflowState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("index")
@Slf4j
@EnableBinding(IndexStream.class)
@Service
public class IndexService {

  private static final ObjectMapper MAPPER =
      new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .registerModule(getOffsetDateTimeModule())
          .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
          .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
          .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

  private final RestHighLevelClient esClient;
  private final String workflowIndex;
  private final String taskIndex;

  @Autowired
  public IndexService(
      @NonNull RestHighLevelClient esClient,
      @NonNull ElasticsearchProperties elasticsearchProperties) {
    this.esClient = esClient;
    this.workflowIndex = elasticsearchProperties.getWorkflowIndex();
    this.taskIndex = elasticsearchProperties.getTaskIndex();
  }

  @StreamListener(IndexStream.WORKFLOW)
  public void indexWorkflow(JsonNode event) throws Exception{
    log.debug("workflow event: {}", event);
    // Convert nextflow workflow event to workflow index doc
    val workflowEvent = MAPPER.treeToValue(event, WorkflowEvent.class);
    val oldDocOpt = getWorkflowDocument(workflowEvent.getRunName());
    val doc =
        oldDocOpt.isPresent()
            ? buildWorkflowDocument(workflowEvent, oldDocOpt.get())
            : buildWorkflowDocument(workflowEvent);

    // index document
    indexWorkflowDocIfNewVersion(doc);
  }

  @SneakyThrows
  @StreamListener(IndexStream.TASK)
  public void indexTask(JsonNode event) {
    log.debug("task event: {}", event);
    // Convert nextflow task event to task index doc
    val taskEvent = MAPPER.treeToValue(event, TaskEvent.class);
    val doc = NextflowDocumentConverter.buildTaskDocument(taskEvent);

    // serialize index objects to json
    val docId = String.format("%s-%d", doc.getRunId(), doc.getTaskId());
    val jsonNode = MAPPER.convertValue(doc, JsonNode.class);

    // Log and index
    log.debug("Indexing task {} for run: {}", doc.getTaskId(), doc.getRunId());
    val request = new IndexRequest(taskIndex);
    request.id(docId);
    request.source(MAPPER.writeValueAsBytes(jsonNode), XContentType.JSON);
    request.versionType(VersionType.EXTERNAL);
    request.version(doc.getState().ordinal());
    try {
      val indexResponse = esClient.index(request, RequestOptions.DEFAULT);
      log.debug(indexResponse.toString());
    } catch (ElasticsearchStatusException e) {
      log.error(
          "Out of order, already have newer version for task {} in run {}, exception: {}",
          doc.getTaskId(),
          doc.getRunId(),
          e.getLocalizedMessage());
    }
  }

  @SneakyThrows
  @StreamListener(IndexStream.WF_MGMT_WORKFLOW)
  public void indexWfMgmtWorkflowEvent(JsonNode event) {
    log.debug("workflow mgmt event: {}", event);
    val mgmtEvent = MAPPER.convertValue(event, WfManagementEvent.class);

    val runid = mgmtEvent.getRunId();

    log.debug("Indexing wf-mgmt event with runId: {}", runid);

    val workflowDocOpt = getWorkflowDocument(runid);
    WorkflowDocument workflowDoc;

    // WF-Mgmt doesn't set Run to Running so don't set start time here, workflow stream handles that
    if (workflowDocOpt.isPresent()) {
      workflowDoc = workflowDocOpt.get();
      workflowDoc.setState(mgmtEvent.getEvent());
    } else {
      log.debug("No document exists with runId: {}", runid);
      workflowDoc =
          WorkflowDocument.builder()
              .runId(runid)
              .repository(mgmtEvent.getWorkflowUrl())
              .parameters(mgmtEvent.getWorkflowParams())
              .engineParameters(mgmtEvent.getWorkflowEngineParams())
              .state(mgmtEvent.getEvent())
              .build();
    }

    // SYSTEM_ERROR is the only terminal event generated by WF-MGMT, and it needs a completed time
    if (mgmtEvent.getEvent() == WorkflowState.SYSTEM_ERROR) {
      if (workflowDoc.getStartTime() != null) {
        val completeTime = OffsetDateTime.parse(event.path("utcTime").asText());
        workflowDoc.setCompleteTime(completeTime.toInstant());
        workflowDoc.setDuration(
            Duration.between(workflowDoc.getStartTime(), completeTime).toMillis());
      }

      workflowDoc.setSuccess(false);
    }

    indexWorkflowDocIfNewVersion(workflowDoc);
  }

  @SneakyThrows
  private void indexWorkflowDocIfNewVersion(WorkflowDocument doc) {
    // serialize index objects to json
    val jsonNode = MAPPER.convertValue(doc, JsonNode.class);

    // Log and index
    log.debug(
        format(
            "Indexing workflow information for run with runId: { %s }, sessionId: { %s }",
            doc.getRunId(), doc.getSessionId()));

    log.debug("Indexing workflow: {}, {}, {}", doc.getRunId(), doc.getState(),doc.getState().ordinal());

    // Log and index
    val request = new IndexRequest(workflowIndex);
    request.id(doc.getRunId());
    request.source(MAPPER.writeValueAsBytes(jsonNode), XContentType.JSON);
    request.versionType(VersionType.EXTERNAL);
    request.version(doc.getState().ordinal());
    try {
      //val indexResponse = esClient.index(request, RequestOptions.DEFAULT);
      log.debug(esClient.index(request, RequestOptions.DEFAULT).toString());
      //log.debug(indexResponse.toString());
    } catch (ElasticsearchStatusException e) {
      log.error(
          "Out of order, already have newer version for run {}, exception: {}, version: {}",
          doc.getRunId(),
          e.getLocalizedMessage(),
          doc.getState().ordinal());
    }
  }

  private Optional<WorkflowDocument> getWorkflowDocument(String runId) {
    try {
      val getRequest = new GetRequest(workflowIndex, runId);
      val getResponse = esClient.get(getRequest, RequestOptions.DEFAULT);
      if (getResponse.isExists()) {
        return Optional.of(
            MAPPER.convertValue(getResponse.getSourceAsMap(), WorkflowDocument.class));
      } else {
        return Optional.empty();
      }
    } catch (Exception e) {
      log.error(e.getLocalizedMessage());
      return Optional.empty();
    }
  }
}
