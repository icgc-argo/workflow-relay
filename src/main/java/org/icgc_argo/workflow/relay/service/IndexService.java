package org.icgc_argo.workflow.relay.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.icgc_argo.workflow.relay.config.elastic.ElasticsearchProperties;
import org.icgc_argo.workflow.relay.config.stream.IndexStream;
import org.icgc_argo.workflow.relay.entities.index.WorkflowState;
import org.icgc_argo.workflow.relay.entities.nextflow.TaskEvent;
import org.icgc_argo.workflow.relay.entities.nextflow.WorkflowEvent;
import org.icgc_argo.workflow.relay.util.NextflowDocumentConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static org.icgc_argo.workflow.relay.util.OffsetDateTimeDeserializer.getOffsetDateTimeModule;

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

    // serialize index objects to json
    val jsonNode = MAPPER.convertValue(doc, JsonNode.class);

    // Log and index
    log.info("Indexing task information for run: {}", doc.getRunId());
    val request = new IndexRequest(taskIndex);
    request.source(MAPPER.writeValueAsBytes(jsonNode), XContentType.JSON);
    esClient.index(request, RequestOptions.DEFAULT);
  }
}
