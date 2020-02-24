package org.icgc_argo.workflow.relay.config.elastic;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("index")
@Data
@Configuration
@ConfigurationProperties(prefix = "elastic")
public class ElasticsearchProperties {

  String host;
  Integer port;
  String userName;
  String password;
  boolean useAuthentication;
  String workflowIndex;
  String taskIndex;
}
