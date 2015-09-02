package com.app.designmore.retrofit.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Joker on 2015/9/1.
 */
public class Address {

  private String userId;
  private String addressId;
  private String addressName;
  private String userName;
  private String address;
  private String mobile;
  private boolean isChecked;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getAddressId() {
    return addressId;
  }

  public void setAddressId(String addressId) {
    this.addressId = addressId;
  }

  public String getAddressName() {
    return addressName;
  }

  public void setAddressName(String addressName) {
    this.addressName = addressName;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getMobile() {
    return mobile;
  }

  public void setMobile(String mobile) {
    this.mobile = mobile;
  }

  public boolean getChecked() {
    return isChecked;
  }

  public void setChecked(boolean isChecked) {
    this.isChecked = isChecked;
  }

  public Address newInstance() {

    try {
      return (Address) super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override protected Object clone() throws CloneNotSupportedException {
    Address address = (Address) super.clone();
    return address;
  }

  @Override public String toString() {
    return "Address{" +
        "userId='" + userId + '\'' +
        ", addressId='" + addressId + '\'' +
        ", addressName='" + addressName + '\'' +
        ", userName='" + userName + '\'' +
        ", address='" + address + '\'' +
        ", mobile='" + mobile + '\'' +
        '}';
  }
}
