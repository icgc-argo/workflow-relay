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

package org.icgc_argo.workflow.relay.service;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static org.icgc_argo.workflow.relay.model.index.TaskState.isNextState;
import static org.icgc_argo.workflow.relay.util.OffsetDateTimeDeserializer.getOffsetDateTimeModule;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.icgc_argo.workflow.relay.config.elastic.ElasticsearchProperties;
import org.icgc_argo.workflow.relay.config.stream.IndexStream;
import org.icgc_argo.workflow.relay.model.index.TaskDocument;
import org.icgc_argo.workflow.relay.model.index.TaskState;
import org.icgc_argo.workflow.relay.model.index.WorkflowState;
import org.icgc_argo.workflow.relay.model.nextflow.TaskEvent;
import org.icgc_argo.workflow.relay.model.nextflow.WorkflowEvent;
import org.icgc_argo.workflow.relay.util.NextflowDocumentConverter;
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

  @SneakyThrows
  @StreamListener(IndexStream.WORKFLOW)
  public void indexWorkflow(JsonNode event) {
    // Convert nextflow workflow event to workflow index doc
    val workflowEvent = MAPPER.treeToValue(event, WorkflowEvent.class);
    val doc = NextflowDocumentConverter.buildWorkflowDocument(workflowEvent);

    // serialize index objects to json
    val jsonNode = MAPPER.convertValue(doc, JsonNode.class);

    // Log and index
    log.info(
        format(
            "Indexing workflow information for run with runId: { %s }, sessionId: { %s }",
            doc.getRunId(), doc.getSessionId()));

    // Check for existing document and do not update if already
    // exists with state WorkflowState.COMPLETE OR WorkflowState.EXECUTOR_ERROR
    GetRequest getRequest = new GetRequest(workflowIndex, doc.getRunId());
    val getResponse = esClient.get(getRequest, RequestOptions.DEFAULT);

    if (getResponse.isExists()
        && Stream.of(valueOf(WorkflowState.COMPLETE), valueOf(WorkflowState.EXECUTOR_ERROR))
            .anyMatch(getResponse.getSourceAsMap().get("state").toString()::equalsIgnoreCase)) {
      log.info(
          format(
              "Skipping document upsert: %s or %s state workflow information for run with runId: { %s }, sessionId: { %s }, already exists in index",
              WorkflowState.COMPLETE,
              WorkflowState.EXECUTOR_ERROR,
              doc.getRunId(),
              doc.getSessionId()));
    } else {
      val request =
          new UpdateRequest(workflowIndex, doc.getRunId())
              .upsert(MAPPER.writeValueAsBytes(jsonNode), XContentType.JSON)
              .doc(MAPPER.writeValueAsBytes(jsonNode), XContentType.JSON);
      esClient.update(request, RequestOptions.DEFAULT);
    }
  }

  @SneakyThrows
  @StreamListener(IndexStream.TASK)
  public void indexTask(JsonNode event) {
    // Convert nextflow task event to task index doc
    val taskEvent = MAPPER.treeToValue(event, TaskEvent.class);
    val doc = NextflowDocumentConverter.buildTaskDocument(taskEvent);

    log.info("Indexing task information for task: {}, in run: {}", doc.getTaskId(), doc.getRunId());

    // Cannot upsert exclusively via elasticsearch as document _id is not known ahead of time
    val existingTaskOpt = checkForExistingTask(doc.getRunId(), doc.getTaskId());
    val taskConsumer = getExistingTaskConsumer(doc);
    existingTaskOpt.ifPresentOrElse(taskConsumer, () -> handleNewTask(doc));
  }

  private Optional<TaskTuple> checkForExistingTask(@NonNull String runId, @NonNull Integer taskId)
      throws IOException {
    // Check for existing document and determine if update is needed
    val bq = new BoolQueryBuilder();
    bq.must(new TermQueryBuilder("runId", runId));
    bq.must(new TermQueryBuilder("taskId", taskId));
    val searchRequest =
        new SearchRequest(taskIndex).source(SearchSourceBuilder.searchSource().query(bq));

    // Execute search and verify number of hits is either 0 or 1. Otherwise Throw illegal state.
    val searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
    val hits = searchResponse.getHits();
    if (hits.getTotalHits().value == 0L) {
      return Optional.empty();
    } else if (hits.getTotalHits().value > 1L) {
      throw new IllegalStateException(
          format("Incorrect number of tasks. Expected 1 or 0, got %d", hits.getTotalHits().value));
    }

    val hit = hits.getHits()[0];
    val docId = hit.getId();

    // Guard against a strange mapping
    val stateObject = hit.getSourceAsMap().get("state");
    if (!(stateObject instanceof String)) {
      throw new IllegalStateException(
          format("Task state is not a string for _id: %s, check index mapping", docId));
    }
    val state = TaskState.fromValue((String) stateObject);

    return Optional.of(new TaskTuple(docId, state));
  }

  /**
   * Create a consumer for handling whether or not to update a new Task document and handle the indexing.
   * Wraps an IOException in a RuntimeException
   * @param doc The new Task Document to be indexed
   * @return A consumer that takes the existing Task information in the form of a taskTuple
   */
  private Consumer<TaskTuple> getExistingTaskConsumer(
      @NonNull TaskDocument doc) {
    return taskTuple -> {
      if (isNextState(taskTuple.getState(), doc.getState())) {
        log.trace(
            "Task {} in Run {} to be updated from {} to {}",
            doc.getTaskId(),
            doc.getRunId(),
            taskTuple.getState(),
            doc.getState());
        try {
          val jsonNode = MAPPER.convertValue(doc, JsonNode.class);
          val request =
              new UpdateRequest(taskIndex, taskTuple.getId())
                  .doc(MAPPER.writeValueAsBytes(jsonNode), XContentType.JSON);
          esClient.update(request, RequestOptions.DEFAULT);
        } catch (IOException io) {
          throw new RuntimeException(io);
        }
      }
    };
  }

  /**
   * Index a new task wrapping a possible IOException as a RuntimeException
   * @param doc The TaskDocument being indexed
   */
  @SneakyThrows
  private void handleNewTask(@NonNull TaskDocument doc) {
    log.trace("New Task {} indexed for Run {}", doc.getTaskId(), doc.getRunId());

    // serialize index objects to json
    val jsonNode = MAPPER.convertValue(doc, JsonNode.class);
    val request = new IndexRequest(taskIndex);
      request.source(MAPPER.writeValueAsBytes(jsonNode), XContentType.JSON);
      esClient.index(request, RequestOptions.DEFAULT);
  }

  @Data
  private static class TaskTuple {
    private final String id;
    private final TaskState state;
  }
}
