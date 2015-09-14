package com.app.designmore.retrofit.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Joker on 2015/9/14.
 */
public class UserInfoEntity extends BaseResponse {

  private String userName;
  private String nickname;
  private String gender;
  private String birthday;
  private String headerUrl;

  public UserInfoEntity(String userName, String birthday, String gender, String headerUrl,
      String nickname) {
    this.userName = userName;
    this.birthday = birthday;
    this.gender = gender;
    this.headerUrl = headerUrl;
    this.nickname = nickname;
  }

  public String getBirthday() {
    return birthday;
  }

  public String getUserName() {
    return userName;
  }

  public String getNickname() {
    return nickname;
  }

  public String getGender() {
    return gender;
  }

  public String getHeaderUrl() {
    return headerUrl;
  }

  @Override public String toString() {
    return "UserInfo{" +
        "userName='" + userName + '\'' +
        ", nickname='" + nickname + '\'' +
        ", gender='" + gender + '\'' +
        ", birthday='" + birthday + '\'' +
        ", headerUrl='" + headerUrl + '\'' +
        '}';
  }
}
