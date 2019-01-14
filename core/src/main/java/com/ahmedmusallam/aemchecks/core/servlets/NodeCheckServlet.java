package com.ahmedmusallam.aemchecks.core.servlets;

import static com.ahmedmusallam.aemchecks.core.util.AdapterUtil.to;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
  service = Servlet.class,
  property = {
      Constants.SERVICE_DESCRIPTION + "=Node Check Servlet",
      ServletResolverConstants.SLING_SERVLET_METHODS + "=" + HttpConstants.METHOD_GET,
      ServletResolverConstants.SLING_SERVLET_EXTENSIONS + "=json",
      ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/aem-checks/node"
  })
public class NodeCheckServlet extends SlingSafeMethodsServlet {

  private static final Logger logger = LoggerFactory.getLogger(NodeCheckServlet.class);

  private static final String SERVICE_NAME = "aem-checks";
  private static final Map<String, Object> AUTH_INFO;

  static {
    AUTH_INFO = Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, (Object) SERVICE_NAME);
  }


  @Reference
  ResourceResolverFactory resourceResolverFactory;

  @Override
  protected void doGet(SlingHttpServletRequest slingRequest, SlingHttpServletResponse slingResponse) throws IOException {
    try (ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(AUTH_INFO)) {
      String type = slingRequest.getParameter("type");
      String path = slingRequest.getParameter("path");
      JsonObject json = new JsonObject();
      switch (Type.fromString(type)) {
        case NODE_EXISTS:
          json.addProperty("result", nodeExists(resourceResolver, path));
          break;
        default:
          json.addProperty("result", false);
          logger.error("nothing to do");
      }
      slingResponse.getWriter().write(json.toString());
    } catch (LoginException e) {
      logger.error("Could not get a ResourceResolver for user {}", SERVICE_NAME, e);
    }
  }

  private boolean nodeExists(ResourceResolver resourceResolver, String path) {

    return Optional.ofNullable(resourceResolver)
        .map(to(Session.class))
        .map(session -> {
          try {
            return session.getNode(path);
          } catch (RepositoryException e) {
            logger.error("well this is unexpected..", e);
            return null;
          }
        })
        .map(Objects::nonNull)
        .orElse(false);
  }
  enum Type {
    NODE_EXISTS, PROPERTIES_EXIST, NOOP;

    public static Type fromString(String action) {
      try {
        action = StringUtils.upperCase(action);
        return Type.valueOf(action);
      } catch (Exception ex) {
        return Type.NOOP;
      }
    }
  }
}
