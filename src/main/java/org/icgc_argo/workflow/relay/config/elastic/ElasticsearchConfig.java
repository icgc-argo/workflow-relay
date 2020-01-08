package org.icgc_argo.workflow.relay.config.elastic;

import lombok.NonNull;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("index")
@Configuration
public class ElasticsearchConfig {

  private ElasticsearchProperties properties;

  @Autowired
  public ElasticsearchConfig(@NonNull ElasticsearchProperties properties) {
    this.properties = properties;
  }

  @Bean
  public RestHighLevelClient restHighLevelClient() {
    return new RestHighLevelClient(
        RestClient.builder(new HttpHost(properties.getHost(), properties.getPort()))
            .setRequestConfigCallback(
                config ->
                    config
                        .setConnectTimeout(15_000)
                        .setConnectionRequestTimeout(15_000)
                        .setSocketTimeout(15_000)));
  }
}
