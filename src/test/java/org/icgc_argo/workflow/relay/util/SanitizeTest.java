package org.icgc_argo.workflow.relay.util;

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Test;

public class SanitizeTest {

  @Test
  @SneakyThrows
  public void testSanitizeString() {
    // ftp://ftp-private.ebi.ac.uk/upload/foivos/test.txt should be hashed to
    // 84b70f172cb4506996c77108036f068f

    val resource = this.getClass().getClassLoader().getResource("fixtures/nextflow_workflow_event.json");
    assert resource != null;
    val content = Files.readString(Path.of(resource.toURI()));

    val mapper = new ObjectMapper();
    val json = mapper.readTree(content);

    Sanitize.sanitize("metadata.parameters.input.path", json);
    val sanitizedInput =
        json.path("metadata").path("parameters").path("input").path("path").asText();
    assertEquals("84b70f172cb4506996c77108036f068f", sanitizedInput);
  }

  @Test
  @SneakyThrows
  public void testSanitizeInteger() {
    val resource = this.getClass().getClassLoader().getResource("fixtures/nextflow_workflow_event.json");
    assert resource != null;
    val content = Files.readString(Path.of(resource.toURI()));

    val mapper = new ObjectMapper();
    val json = mapper.readTree(content);

    Sanitize.sanitize("metadata.parameters.input.superSecretNumber", json);
    val sanitizedInput =
        json.path("metadata").path("parameters").path("input").path("superSecretNumber").asText();
    assertEquals("e4da3b7fbbce2345d7772b0674a318d5", sanitizedInput);
  }

  @Test
  @SneakyThrows
  public void testSanitizeNumber() {
    val resource = this.getClass().getClassLoader().getResource("fixtures/nextflow_workflow_event.json");
    assert resource != null;
    val content = Files.readString(Path.of(resource.toURI()));

    val mapper = new ObjectMapper();
    val json = mapper.readTree(content);

    Sanitize.sanitize("metadata.parameters.input.otherSuperSecretNumber", json);
    val sanitizedInput =
        json.path("metadata")
            .path("parameters")
            .path("input")
            .path("otherSuperSecretNumber")
            .asText();
    assertEquals("8e7b37489419db887910eb6034a937cd", sanitizedInput);
  }

  @Test
  public void testMD5_1() {
    val inputString = "WITNESS ME";
    val hash = "e6c37c5a7b42299ae6c4aebf568e031a"; // echo -n "WITNESS ME" | md5sum

    val output = Sanitize.md5(inputString);
    assertEquals(hash, output);
  }

  @Test
  public void testMD5_2() {
    val inputString = "25";
    val hash = "8e296a067a37563370ded05f5a3bf3ec"; // echo -n 25 | md5sum

    val output = Sanitize.md5(inputString);
    assertEquals(hash, output);
  }
}
