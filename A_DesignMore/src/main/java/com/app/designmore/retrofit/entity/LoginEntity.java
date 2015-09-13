package com.app.designmore.retrofit.entity;

/**
 * Created by Joker on 2015/9/13.
 */
public class LoginEntity {

  private Long _id;
  private String userId;
  private String addressId;

  public LoginEntity(String userId, String addressId) {
    this.userId = userId;
    this.addressId = addressId;
  }

  public String getUserId() {
    return userId;
  }

  public String getAddressId() {
    return addressId;
  }

  public Long get_id() {
    return _id;
  }
}
