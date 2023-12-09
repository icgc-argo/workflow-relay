package org.icgcargo.workflow.relay.util;

import org.apache.commons.lang3.ObjectUtils;

public class ObjectUtilities {

  public static <T> T defaultIfNullOrEmpty(final T object, final T defaultValue){
    return ObjectUtils.isNotEmpty(object)?object:defaultValue;
  }

}
