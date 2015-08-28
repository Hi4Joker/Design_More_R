package com.app.designmore.greendao.entity;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Joker on 2015/8/24.
 */
public class UserInfoEntity {

  /* new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();*/
  private Long _id;

  @Expose @SerializedName("user_id") private String userId;
  @Expose @SerializedName("user_name") private String userName;
  @Expose @SerializedName("mobile_phone") private String phone;
  @Expose private String sex;
  @Expose private String email;

  public UserInfoEntity() {

  }

  public UserInfoEntity(Long _id, String userId, String userName, String phone, String sex,
      String email) {
    this._id = _id;

    this.userId = userId;
    this.userName = userName;
    this.phone = phone;
    this.sex = sex;
    this.email = email;
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

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getSex() {
    return sex;
  }

  public void setSex(String sex) {
    this.sex = sex;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
