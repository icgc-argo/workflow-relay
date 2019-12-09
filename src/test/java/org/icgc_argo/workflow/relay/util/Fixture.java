package org.icgc_argo.workflow.relay.util;

import static org.icgc_argo.workflow.relay.util.OffsetDateTimeDeserializer.getOffsetDateTimeModule;
import static org.icgc_argo.workflow.relay.util.StringUtilities.inputStreamToString;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Fixture {

  private static final String BASE_PATH = "fixtures" + File.separator;

  @SneakyThrows
  public static <T> T loadJsonFixture(
      Class clazz, String fileName, Class<T> targetClass, ObjectMapper mapper) {
    String json = loadJsonString(clazz, fileName);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.registerModule(new JavaTimeModule());
    mapper.registerModule(getOffsetDateTimeModule());

    return mapper.readValue(json, targetClass);
  }

  public static String loadJsonString(Class clazz, String fileName) throws IOException {
    return inputStreamToString(
        Optional.ofNullable(clazz.getClassLoader().getResource(BASE_PATH + fileName))
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "fixture not found. make sure you created the correct "
                            + "folder if this is a new class or if you renamed the class"))
            .openStream());
  }
}
