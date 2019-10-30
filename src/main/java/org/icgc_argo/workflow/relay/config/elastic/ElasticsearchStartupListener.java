package org.icgc_argo.workflow.relay.config.elastic;

import static org.icgc_argo.workflow.relay.util.StringUtilities.inputStreamToString;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Profile("index")
@Slf4j
@Component
public class ElasticsearchStartupListener implements ApplicationListener<ContextRefreshedEvent> {

  private final RestHighLevelClient client;
  private final ElasticsearchProperties properties;

  @Value("classpath:run_log_mapping.json")
  private Resource workflowIndexMapping;

  @Value("classpath:task_log_mapping.json")
  private Resource taskIndexMapping;

  @Autowired
  public ElasticsearchStartupListener(
      @NonNull RestHighLevelClient client, @NonNull ElasticsearchProperties properties) {
    this.client = client;
    this.properties = properties;
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    log.info("Ensuring index initialization...");
    val workflow = ensureIndex(properties.workflowIndex);
    val task = ensureIndex(properties.taskIndex);
    if (!workflow || !task) {
      log.error("Could not ensure required indices. Workflow: {}, Task: {}", workflow, task);
      SpringApplication.exit(event.getApplicationContext(), () -> 1);
    }
  }

  @SneakyThrows
  private boolean ensureIndex(String indexName) {
    val request = new GetIndexRequest(indexName);
    if (!client.indices().exists(request, RequestOptions.DEFAULT)) {
      val indexSource = loadIndexSourceAsString(indexName);
      val createRequest = new CreateIndexRequest(indexName);
      log.info("Creating index {}", indexName);
      createRequest.source(indexSource, XContentType.JSON);
      val response = client.indices().create(createRequest, RequestOptions.DEFAULT);
      return response.isAcknowledged();
    }
    return true;
  }

  @SneakyThrows
  private String loadIndexSourceAsString(String indexName) {
    log.trace("in loadIndexSourceAsString: {}", indexName);
    if (indexName.equals(properties.workflowIndex)) {
      return inputStreamToString(workflowIndexMapping.getInputStream());
    } else if (indexName.equals(properties.taskIndex)) {
      return inputStreamToString(taskIndexMapping.getInputStream());
    } else
      throw new RuntimeException(
          "Failed to load index source: index name must be workflow or task.");
  }
}
