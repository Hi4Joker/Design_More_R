package com.app.designmore.retrofit.entity;

import retrofit.http.PUT;

/**
 * Created by Administrator on 2015/9/8.
 */
public class LoginCodeEntity {

  private String code;

  public LoginCodeEntity(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
