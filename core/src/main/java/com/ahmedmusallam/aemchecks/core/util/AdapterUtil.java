package com.ahmedmusallam.aemchecks.core.util;

import static java.util.Optional.ofNullable;

import java.util.function.Function;
import javax.jcr.Session;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.ResourceResolver;

public class AdapterUtil {


  public static <AdapterType> AdapterType adaptTo(Adaptable adaptable, Class<AdapterType> type) {
    return ofNullable(adaptable)
        .map(tempAdaptable -> tempAdaptable.adaptTo(type))
        .orElse(null);
  }

  public static Session toSession(ResourceResolver resourceResolver) {
    return adaptTo(resourceResolver, Session.class);
  }

}
