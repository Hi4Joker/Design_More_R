package com.app.designmore.retrofit.response;

import com.google.gson.annotations.Expose;

/**
 * Created by Joker on 2015/9/19.
 */
public class TestResponse {

  /* "code": 400,
    "message": "成功得到数据",
    "data": {
        "username": "您好",
        "password": "123456",
        "addtime": 1442657596
    }*/

  @Expose private String code;
  @Expose private String message;
  @Expose private Data data;

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public Data getData() {
    return data;
  }

  public class Data {

    @Expose public String username;
    @Expose public String password;
    @Expose public String addtime;

    @Override public String toString() {
      return "Data{" +
          "username='" + username + '\'' +
          ", password='" + password + '\'' +
          ", addtime='" + addtime + '\'' +
          '}';
    }
  }

  @Override public String toString() {
    return "TestResponse{" +
        "code='" + code + '\'' +
        ", message='" + message + '\'' +
        ", data=" + data +
        '}';
  }
}
