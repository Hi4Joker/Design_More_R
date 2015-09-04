package com.app.designmore.retrofit.response;

import com.google.gson.annotations.Expose;
import java.util.List;

/**
 * Created by Joker on 2015/9/4.
 */
public class SearchListResponse extends BaseResponse {

  @Expose List<String> result;

  public List<String> getResult() {
    return result;
  }
}
