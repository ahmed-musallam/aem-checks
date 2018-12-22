package com.ahmedmusallam.aemchecks.core.util;

import java.util.Optional;
import java.util.function.Function;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeUtils {

  private static final Logger logger = LoggerFactory.getLogger(NodeUtils.class);

  public static Node getNode(String path, ResourceResolver resolver) {
    return Optional.ofNullable(AdapterUtil.toSession(resolver))
        .map(NodeUtils.getNode(path))
        .orElse(null);
  }

  static Function<Session, Node> getNode(String path) {
    return session -> {
      try {
        return session.getNode(path);
      } catch (RepositoryException e) {
        // for the sake of not keeping it empty :)
        logger.debug("Failed to get node at path: {}", path, e);
      }
      return null;
    };
  }
}
