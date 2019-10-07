package org.icgc_argo.workflow.relay.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.icgc_argo.workflow.relay.config.elastic.ElasticsearchProperties;
import org.icgc_argo.workflow.relay.config.stream.IndexStream;
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

  private static ObjectMapper MAPPER = new ObjectMapper();

  private RestHighLevelClient esClient;
  private String workflowIndex;
  private String taskIndex;

  @Autowired
  public IndexService(
      RestHighLevelClient esClient, ElasticsearchProperties elasticsearchProperties) {
    this.esClient = esClient;
    this.workflowIndex = elasticsearchProperties.getWorkflowIndex();
    this.taskIndex = elasticsearchProperties.getTaskIndex();
  }

  @SneakyThrows
  @StreamListener(IndexStream.WORKFLOW)
  public void indexWorkflow(JsonNode event) {
    // TODO: Generalize event information parsing
    val id = event.path("runId").asText();
    val request =
        new UpdateRequest(workflowIndex, id)
            .upsert(MAPPER.writeValueAsBytes(event), XContentType.JSON)
            .doc(
                MAPPER.writeValueAsBytes(event),
                XContentType.JSON); // TODO: Handle these exceptions
    esClient.updateAsync(
        request,
        RequestOptions.DEFAULT,
        new ActionListener<>() {

          @Override
          public void onResponse(UpdateResponse updateResponse) {
            log.info("Indexed workflow run: {}", id);
          }

          @Override
          public void onFailure(Exception e) {
            log.error("Could not handle workflow: {}", event, e);
          }
        });
  }

  @SneakyThrows
  @StreamListener(IndexStream.TASK)
  public void indexTask(JsonNode event) {
    // TODO: Generalize event information parsing
    val id = event.path("runId").asText();
    val request = new IndexRequest(taskIndex);
    request.source(
        MAPPER.writeValueAsBytes(event), XContentType.JSON); // TODO: Handle these exceptions
    esClient.indexAsync(
        request,
        RequestOptions.DEFAULT,
        new ActionListener<>() {
          @Override
          public void onResponse(IndexResponse indexResponse) {
            log.info("Indexed task for: {}", id);
          }

          @Override
          public void onFailure(Exception e) {
            log.error("Could not handle task: {}", event, e);
          }
        });
  }
}
