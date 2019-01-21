package com.ahmedmusallam.aemchecks.core.servlets;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;

public abstract class UniformJsonResponseServlet extends SlingSafeMethodsServlet {

  protected abstract void get(
      SlingHttpServletRequest slingRequest,
      SlingHttpServletResponse slingResponse,
      JsonResponse jsonResponse);

  protected abstract List<String> checkRequestValidity(
      SlingHttpServletRequest slingRequest,
      SlingHttpServletResponse slingResponse);

  @Override
  protected void doGet(
      SlingHttpServletRequest slingRequest,
      SlingHttpServletResponse slingResponse) throws ServletException, IOException {

    JsonResponse jsonResponse = new JsonResponse();

    List<String> errors = checkRequestValidity(slingRequest,slingResponse);
    if (errors != null && !errors.isEmpty()) {
      errors
          .stream()
          .forEach(jsonResponse::addError);
      slingResponse.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
    } else {
      get(slingRequest, slingResponse, jsonResponse);
    }

    slingResponse.setContentType("application/json");
    slingResponse.getWriter().write(jsonResponse.toJson());
  }
}
