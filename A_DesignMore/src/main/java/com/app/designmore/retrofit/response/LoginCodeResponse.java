package com.app.designmore.retrofit.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Created by Joker on 2015/9/8.
 */
public class LoginCodeResponse extends BaseResponse {


  /*{"result":"09581","code":1,"message":"发送成功"}*/

  @Expose @SerializedName("result") String code;

  public String getCode() {
    return code;
  }

  @Override public String toString() {
    return "LoginCodeResponse{" +
        "code='" + code + '\'' +
        '}';
  }
}
