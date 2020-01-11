package org.icgc_argo.workflow.relay.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigInteger;
import java.security.MessageDigest;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

public class Sanitize {

  public static void sanitize(@NonNull String jsonPath, @NonNull JsonNode json) {
    val paths = jsonPath.split("\\.");

    // Maintain parent node as we traverse since jackson is optimized for mem and doesn't doubly
    // link
    JsonNode parent = null;
    JsonNode acc = json;
    for (val path : paths) {
      if (acc.isMissingNode()) return; // Short circuit
      parent = acc;
      acc = acc.path(path);
    }

    if (acc.isValueNode()) {
      val hash = md5(acc.asText());
      if (parent == null) {
        return; // Guard and short return
      }
      ((ObjectNode) parent).put(paths[paths.length - 1], hash);
    }
  }

  @SneakyThrows
  static String md5(String text) {
    val messageDigest = MessageDigest.getInstance("MD5");
    messageDigest.update(text.getBytes());
    val digest = messageDigest.digest();
    val number = new BigInteger(1, digest);
    return String.format("%032x", number);
  }
}
