package org.icgc_argo.workflow.relay.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.val;

/** Utility class that converts "start" and "complete" json objects to OffsetDateTime. */
public class OffsetDateTimeDeserializer extends StdDeserializer<OffsetDateTime> {

  public OffsetDateTimeDeserializer() {
    this(null);
  }

  public OffsetDateTimeDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public OffsetDateTime deserialize(JsonParser jp, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    JsonNode node = jp.getCodec().readTree(jp);
    val offsetId = node.get("offset").get("id").textValue();
    val zoneOffSet = ZoneOffset.of(offsetId);

    return OffsetDateTime.of(
        node.get("year").asInt(),
        node.get("monthValue").asInt(),
        node.get("dayOfMonth").asInt(),
        node.get("hour").asInt(),
        node.get("minute").asInt(),
        node.get("second").asInt(),
        node.get("nano").asInt(),
        zoneOffSet);
  }

  public static SimpleModule getModule() {
    val module = new SimpleModule();
    return module.addDeserializer(OffsetDateTime.class, new OffsetDateTimeDeserializer());
  }
}
