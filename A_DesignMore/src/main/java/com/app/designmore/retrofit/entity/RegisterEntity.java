package com.app.designmore.retrofit.entity;

/**
 * Created by Joker on 2015/9/9.
 */
public class RegisterEntity {
  private String registerMessage;

  public RegisterEntity(String registerMessage) {
    this.registerMessage = registerMessage;
  }

  public String getRegisterMessage() {

    return registerMessage;
  }
}
