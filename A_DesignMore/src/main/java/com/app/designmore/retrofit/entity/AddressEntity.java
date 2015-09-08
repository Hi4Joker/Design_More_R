package com.app.designmore.retrofit.entity;

import java.io.Serializable;

/**
 * Created by Joker on 2015/9/1.
 */
public class AddressEntity implements Serializable, Cloneable {

  protected String addressId;
  protected String userName;
  protected String province;
  protected String city;
  protected String address;
  protected String mobile;
  protected String zipcode;
  protected String isDefault;

  @Override public boolean equals(Object obj) {

    if (obj == null || this.getClass() != obj.getClass() || !this.getAddressId()
        .equals(((AddressEntity) obj).getAddressId())) {
      return false;
    }

    return true;
  }

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

  public String isDefault() {
    return isDefault;
  }

  public void setDefault(String isDefault) {
    this.isDefault = isDefault;
  }

  public AddressEntity newInstance() {

    try {
      return (AddressEntity) super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override protected Object clone() throws CloneNotSupportedException {
    AddressEntity address = (AddressEntity) super.clone();
    return address;
  }

  @Override public String toString() {
    return "AddressEntity{" +
        "addressId='" + addressId + '\'' +
        ", userName='" + userName + '\'' +
        ", province='" + province + '\'' +
        ", city='" + city + '\'' +
        ", address='" + address + '\'' +
        ", mobile='" + mobile + '\'' +
        ", zipcode='" + zipcode + '\'' +
        ", isDefault=" + isDefault +
        '}';
  }
}
