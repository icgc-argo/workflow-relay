package org.icgc_argo.workflow.relay.service;

import static java.lang.String.format;
import static org.icgc_argo.workflow.relay.util.OffsetDateTimeDeserializer.getOffsetDateTimeModule;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Base64;
import javax.swing.*;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.icgc_argo.workflow.relay.config.elastic.ElasticsearchProperties;
import org.icgc_argo.workflow.relay.config.stream.IndexStream;
import org.icgc_argo.workflow.relay.entities.metadata.TaskEvent;
import org.icgc_argo.workflow.relay.entities.metadata.WorkflowEvent;
import org.icgc_argo.workflow.relay.util.DocumentConverter;
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
  private final String userName;
  private final String password;

  @Autowired
  public IndexService(
      @NonNull RestHighLevelClient esClient,
      @NonNull ElasticsearchProperties elasticsearchProperties) {
    this.esClient = esClient;
    this.workflowIndex = elasticsearchProperties.getWorkflowIndex();
    this.taskIndex = elasticsearchProperties.getTaskIndex();
    this.userName = elasticsearchProperties.getUserName();
    this.password = elasticsearchProperties.getPassword();
  }

  @SneakyThrows
  @StreamListener(IndexStream.WORKFLOW)
  public void indexWorkflow(JsonNode event) {
    val workflowEvent = MAPPER.treeToValue(event, WorkflowEvent.class);

    // convert metadata objects to index objects
    val doc = DocumentConverter.buildWorkflowDocument(workflowEvent);

    // serialize index objects to json
    val jsonNode = MAPPER.convertValue(doc, JsonNode.class);

    val runId = event.path("runId").asText();
    val runName = event.path("runName").asText();
    log.info(
        format(
            "Indexing workflow information for run with runName: { %s }, runId: { %s }",
            runName, runId));
    val request =
        new UpdateRequest(workflowIndex, runName)
            .upsert(MAPPER.writeValueAsBytes(jsonNode), XContentType.JSON)
            .doc(MAPPER.writeValueAsBytes(jsonNode), XContentType.JSON);
    val token = Base64.getEncoder().encode((userName + ":" + password).getBytes());
    esClient.update(
        request,
        RequestOptions.DEFAULT.toBuilder().addHeader("Authorization", token.toString()).build());
  }

  @SneakyThrows
  @StreamListener(IndexStream.TASK)
  public void indexTask(JsonNode event) {
    val taskEvent = MAPPER.treeToValue(event, TaskEvent.class);
    val doc = DocumentConverter.buildTaskDocument(taskEvent);
    val jsonNode = MAPPER.convertValue(doc, JsonNode.class);
    val id = event.path("runId").asText();
    log.info("Indexing task information for run: {}", id);
    val request = new IndexRequest(taskIndex);
    request.source(MAPPER.writeValueAsBytes(jsonNode), XContentType.JSON);
    esClient.index(request, RequestOptions.DEFAULT);
  }
}
