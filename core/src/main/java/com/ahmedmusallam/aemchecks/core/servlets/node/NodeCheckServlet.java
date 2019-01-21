package com.ahmedmusallam.aemchecks.core.servlets.node;

import static com.ahmedmusallam.aemchecks.core.util.AdapterUtil.to;

import com.ahmedmusallam.aemchecks.core.servlets.JsonResponse;
import com.ahmedmusallam.aemchecks.core.servlets.UniformJsonResponseServlet;
import com.ahmedmusallam.aemchecks.core.util.NodeUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.Servlet;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.ServletResolverConstants;
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
public class NodeCheckServlet extends UniformJsonResponseServlet {

  private static final Logger logger = LoggerFactory.getLogger(NodeCheckServlet.class);

  private static final String SERVICE_NAME = "aem-checks";
  private static final Map<String, Object> AUTH_INFO =
      Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, SERVICE_NAME);


  @Reference
  ResourceResolverFactory resourceResolverFactory;

  @Override
  protected void get(
      SlingHttpServletRequest slingRequest,
      SlingHttpServletResponse slingResponse,
      JsonResponse jsonResponse) {
    String type = getType(slingRequest);
    String path = getPath(slingRequest);

    try (ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(AUTH_INFO)){
      switch (Type.fromString(type)) {
        case NODE_EXISTS:
          handleNodeExists(resourceResolver,path, jsonResponse);
          break;
        case PROPERTIES_EXIST: {
          handlePropertiesExist(resourceResolver, path, getProperties(slingRequest), jsonResponse);
          break;
        }
        default:
          jsonResponse.addError("Missing required Parameter: [type]");
      }
    } catch (LoginException e) {
      String msg = "Could not get a ResourceResolver for user " + SERVICE_NAME ;
      logger.error(msg, e);
      jsonResponse.addError(msg);
    }
  }

  @Override
  protected List<String> checkRequestValidity(SlingHttpServletRequest slingRequest,
      SlingHttpServletResponse slingResponse) {
      List<String> errors = new ArrayList<>();
      String type = getType(slingRequest);
      String path = getPath(slingRequest);

      if(Type.NOOP.equals(Type.fromString(type))) {
        errors.add("Value of parameter type, must be one of: node_exists, properties_exist");
      }

      if(StringUtils.isBlank(path)) {
        errors.add("Missing required parameter 'path'");
      }
      return errors;
  }

  private void handlePropertiesExist(ResourceResolver resourceResolver, String path, List<Prop> properties, JsonResponse jsonResponse){
    JsonObject jsonObject = new JsonObject();

    boolean result = properties
        .stream()
        .map(prop ->
            NodeUtils.hasProperty(resourceResolver, path, prop.getName(), prop.getValue()))
        .filter(check -> BooleanUtils.isFalse(check))
        .findFirst()
        .orElse(true);

    jsonObject.addProperty("result", result);
    jsonResponse.setData(jsonObject);
  }

  private void handleNodeExists(ResourceResolver resourceResolver, String path, JsonResponse jsonResponse){
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("result", NodeUtils.nodeExists(resourceResolver, path));
    jsonResponse.setData(jsonObject);
  }

  private String getType(SlingHttpServletRequest slingRequest){
    return slingRequest.getParameter("type");
  }

  private String getPath(SlingHttpServletRequest slingRequest){
    return slingRequest.getParameter("path");
  }

  private List<Prop> getProperties(SlingHttpServletRequest slingRequest){
    String[] properties = Optional.ofNullable(slingRequest)
        .map(request -> request.getParameter("properties"))
        .map(props -> props.split(","))
        .orElse(new String[0]);

    return Arrays.stream(properties)
        .map(Prop::new)
        .collect(Collectors.toList());
  }

  class Prop {
    private String name,value;
    private Prop(String expression) {
      if(StringUtils.contains(expression, "=")){
        String[] parts = expression.split("=");
        this.name = parts[0];
        this.value = parts[1];
      } else {
        this.name = expression;
      }
    }
    public String getName() {
      return name;
    }

    public String getValue() {
      return value;
    }
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
