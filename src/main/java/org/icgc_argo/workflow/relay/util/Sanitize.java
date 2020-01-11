package org.icgc_argo.workflow.relay.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import lombok.val;
import org.springframework.util.DigestUtils;

public class Sanitize {

  public static void sanitize(@NonNull String jsonPath, @NonNull JsonNode json) {
    val paths = jsonPath.split("\\.");

    // Maintain parent node as we traverse since jackson is optimized for mem and doesn't doubly link
    JsonNode parent = null;
    JsonNode acc = json;
    for (val path: paths) {
      parent = acc;
      acc = acc.path(path);
    }

    if (acc.isValueNode()) {
      val hash = md5(acc.asText());
      if (parent == null) {
        return; // Guard and short return
      }
      ((ObjectNode) parent).put(paths[paths.length-1], hash);
    }
  }

  static String md5(String text) {
    val digest = DigestUtils.md5Digest(text.getBytes());
    StringBuilder sb = new StringBuilder();
    for (byte b : digest) {
      sb.append(Integer.toHexString((b & 0xff)));
    }
    return sb.toString();
  }

}
