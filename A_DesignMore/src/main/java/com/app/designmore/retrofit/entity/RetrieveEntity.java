package com.app.designmore.retrofit.entity;

/**
 * Created by Joker on 2015/9/12.
 */
public class RetrieveEntity {

  private int retrieveCode;
  private String retrieveMessage;

  public RetrieveEntity(int retrieveCode, String retrieveMessage) {
    this.retrieveCode = retrieveCode;
    this.retrieveMessage = retrieveMessage;
  }

  public int getRegisterCode() {
    return retrieveCode;
  }

  public String getRegisterMessage() {

    return retrieveMessage;
  }
}
