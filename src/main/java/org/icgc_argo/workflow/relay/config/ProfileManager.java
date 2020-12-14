package org.icgc_argo.workflow.relay.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class ProfileManager {
  @Value("${spring.profiles.active:}")
  private String activeProfiles;

  public List<String> getActiveProfiles() {
    return Arrays.asList(activeProfiles.split(","));
  }
}
