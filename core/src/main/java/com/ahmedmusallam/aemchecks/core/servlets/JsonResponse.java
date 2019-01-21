package com.ahmedmusallam.aemchecks.core.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.List;

/** A response POJO that contains error and data. To be serialized by Gson for JSON responses. */
public class JsonResponse {

  private transient final Gson gson = new Gson();
  private JsonElement data;
  private List<String> errors;

  /** Returns error object. Should be null if no error. */
  public List<String> getErrors() {
    return errors;
  }

  /** Set error object. */
  public void setErrors(List<String> errors) {
    this.errors = errors;
  }

  /** Returns the data as a JsonElement. (can be object, array or primitive ) */
  public JsonElement getData() {
    return data;
  }

  public void setData(JsonElement data) {
    this.data = data;
  }

  public List<String> addError(String error) {
    List<String> array = getCreateErrors();
    array.add(error);
    return array;
  }

  public String toJson() {
    return gson.toJson(this);
  }

  private List<String> getCreateErrors(){
    if (errors == null) {
      errors = new ArrayList<>();
    }
    return errors;
  }
}