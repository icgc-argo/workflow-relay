package org.icgc_argo.workflow.relay.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

public class StringUtilities {

  /** loads a string out of input stream. */
  @SneakyThrows
  public static String inputStreamToString(@NonNull InputStream inputStream) {
    try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
      val buffer = new byte[1024];
      int length;
      while ((length = inputStream.read(buffer)) != -1) {
        result.write(buffer, 0, length);
      }
      return result.toString(UTF_8);
    }
  }
}
