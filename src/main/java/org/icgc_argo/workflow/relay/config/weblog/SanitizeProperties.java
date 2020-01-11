package org.icgc_argo.workflow.relay.config.weblog;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("weblog")
@Data
@Configuration
@ConfigurationProperties(prefix = "sanitize")
public class SanitizeProperties {
  List<String> paths;
}
