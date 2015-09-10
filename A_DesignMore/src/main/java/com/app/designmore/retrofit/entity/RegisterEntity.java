package com.app.designmore.retrofit.entity;

/**
 * Created by Joker on 2015/9/9.
 */
public class RegisterEntity {

  private int registerCode;
  private String registerMessage;

  public RegisterEntity(int registerCode, String registerMessage) {
    this.registerCode = registerCode;
    this.registerMessage = registerMessage;
  }

  public int getRegisterCode() {
    return registerCode;
  }

  public String getRegisterMessage() {

    return registerMessage;
  }
}
