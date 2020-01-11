package org.icgc_argo.workflow.relay.config.weblog;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Profile("weblog")
@Data
@Configuration
@ConfigurationProperties(prefix = "sanitize")
public class SanitizeProperties {
  List<String> paths;
}

