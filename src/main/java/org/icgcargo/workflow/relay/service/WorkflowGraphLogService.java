package org.icgcargo.workflow.relay.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.codec.digest.DigestUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.icgcargo.workflow.relay.config.elastic.ElasticsearchProperties;
import org.icgcargo.workflow.relay.config.stream.GraphLogStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("graphlog")
@Slf4j
@EnableBinding(GraphLogStream.class)
@Service
public class WorkflowGraphLogService {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final RestHighLevelClient esClient;
  private final String graphLogInfoDebugIndex;
  private final String graphLogErrorWarningIndex;

  @Autowired
  public WorkflowGraphLogService(
      @NonNull RestHighLevelClient esClient,
      @NonNull ElasticsearchProperties elasticsearchProperties) {
    this.esClient = esClient;
    this.graphLogInfoDebugIndex = elasticsearchProperties.getGraphLogInfoDebugIndex();
    this.graphLogErrorWarningIndex = elasticsearchProperties.getGraphLogErrorWarningIndex();
  }

  @StreamListener(GraphLogStream.INFODEBUG)
  public void indexInfoDebug(JsonNode event) {
    log.debug("WorkflowGraphLogService: INFODEBUG listener called");
    indexGraphLog(graphLogInfoDebugIndex, event);
  }

  @StreamListener(GraphLogStream.WARNINGERROR)
  public void indexWarningError(JsonNode event) {
    log.debug("WorkflowGraphLogService: WARNINGERROR listener called");
    indexGraphLog(graphLogErrorWarningIndex, event);
  }

  @SneakyThrows
  private void indexGraphLog(String index, JsonNode event) {
    log.debug("Indexing GraphLog event into {}: {}", index, event.toString());
    val source = MAPPER.writeValueAsBytes(event);
    val request = new IndexRequest(index);
    request.id(DigestUtils.sha1Hex(source));
    request.source(source, XContentType.JSON);
    val indexResponse = esClient.index(request, RequestOptions.DEFAULT);
    log.debug(indexResponse.toString());
  }
}
