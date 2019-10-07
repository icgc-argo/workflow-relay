package org.icgc_argo.workflow.relay.config.elastic;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Profile("index")
@Slf4j
@Component
public class ElasticsearchStartupListener implements ApplicationListener<ContextRefreshedEvent> {

  private RestHighLevelClient client;
  private ElasticsearchProperties properties;

  @Autowired
  public ElasticsearchStartupListener(
      RestHighLevelClient client, ElasticsearchProperties properties) {
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
      val createRequest = new CreateIndexRequest(indexName);
      log.info("Creating index {}", indexName);
      val response = client.indices().create(createRequest, RequestOptions.DEFAULT);
      return response.isAcknowledged();
    }
    return true;
  }
}
