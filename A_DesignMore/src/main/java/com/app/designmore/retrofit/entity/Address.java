package com.app.designmore.retrofit.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Joker on 2015/9/1.
 */
public class Address {

  private String addressId;
  private String userName;
  private String province;
  private String city;
  private String address;
  private String mobile;
  private String zipcode;
  private boolean isChecked;

  public String getAddressId() {
    return addressId;
  }

  public void setAddressId(String addressId) {
    this.addressId = addressId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getProvince() {
    return province;
  }

  public void setProvince(String province) {
    this.province = province;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
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

  public String getZipcode() {
    return zipcode;
  }

  public void setZipcode(String zipcode) {
    this.zipcode = zipcode;
  }

  public boolean isChecked() {
    return isChecked;
  }

  public void setIsChecked(boolean isChecked) {
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
        "addressId='" + addressId + '\'' +
        ", userName='" + userName + '\'' +
        ", province='" + province + '\'' +
        ", city='" + city + '\'' +
        ", address='" + address + '\'' +
        ", mobile='" + mobile + '\'' +
        ", zipcode='" + zipcode + '\'' +
        ", isChecked=" + isChecked +
        '}';
  }
}
