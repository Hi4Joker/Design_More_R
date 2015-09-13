package com.app.designmore.greendao.entity;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Joker on 2015/8/24.
 */
public class Dao_LoginInfo {

  private Long _id;
  private String userId;
  private String addressId;

  public Dao_LoginInfo(Long _id, String userId, String addressId) {
    this._id = _id;
    this.userId = userId;
    this.addressId = addressId;
  }

  public Long get_id() {
    return _id;
  }

  public void set_id(Long _id) {
    this._id = _id;
  }

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
}
