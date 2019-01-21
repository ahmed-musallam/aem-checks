package com.ahmedmusallam.aemchecks.core.util;

import static com.ahmedmusallam.aemchecks.core.util.AdapterUtil.to;

import com.fasterxml.jackson.databind.annotation.JsonAppend.Prop;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeUtils {

  private static final Logger logger = LoggerFactory.getLogger(NodeUtils.class);

  public static Node getNode(ResourceResolver resolver, String path) {
    return Optional.ofNullable(AdapterUtil.toSession(resolver))
        .map(NodeUtils.getNode(path))
        .orElse(null);
  }

  public static Function<Session, Node> getNode(String path) {
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


  public static boolean nodeExists(ResourceResolver resourceResolver, String path) {
    return Optional.ofNullable(resourceResolver)
        .map(to(Session.class))
        .map(session -> {
          try {
            return session.getNode(path);
          } catch (RepositoryException e) {
            logger.error("Exception while checking if node exists [{}]", path, e);
            return null;
          }
        })
        .map(Objects::nonNull)
        .orElse(false);
  }

  public static boolean hasProperty(ResourceResolver resourceResolver, String path, String propertyName, String propertyValue) {
    return Optional.ofNullable(resourceResolver)
        .map(to(Session.class))
        .map(session -> {
          try {
            return session.getNode(path);
          } catch (RepositoryException e) {
            logger.error("Exception while checking if property exists [{}]", path, e);
            return null;
          }
        })
        .map(node -> {
          try {
            boolean hasIt = node.hasProperty(propertyName);
            if (hasIt && StringUtils.isNotBlank(propertyValue)) {
              Property prop = node.getProperty(propertyName);
              return propertyValue.equals(prop.getString());
            } else {
              return hasIt;
            }
          } catch (RepositoryException e) {
            logger.error("Exception while checking if property exists [{}]", path, e);
            return null;
          }
        })
        .orElse(false);
  }


}
