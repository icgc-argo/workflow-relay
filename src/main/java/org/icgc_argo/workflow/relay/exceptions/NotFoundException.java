package org.icgc_argo.workflow.relay.exceptions;

import static java.lang.String.format;

import lombok.NonNull;

public class NotFoundException extends RuntimeException {

  public NotFoundException(@NonNull String message) {
    super(message);
  }

  public static void checkNotFound(
      boolean expression, @NonNull String formattedMessage, @NonNull Object... args) {
    if (!expression) {
      throw new NotFoundException(format(formattedMessage, args));
    }
  }

  public static NotFoundException buildNotFoundException(
      @NonNull String formattedMessage, Object... args) {
    return new NotFoundException(format(formattedMessage, args));
  }
}