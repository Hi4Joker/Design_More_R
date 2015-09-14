package com.app.designmore.retrofit.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * Created by Joker on 2015/9/14.
 */
public class UserInfoEntity extends BaseResponse implements Serializable {

  private String userName;
  private String nickname;
  private String gender;
  private String birthday;
  private String headerUrl;

  public UserInfoEntity(String userName, String nickname, String gender, String birthday,
      String headerUrl) {
    this.userName = userName;
    this.nickname = nickname;
    this.gender = gender;
    this.birthday = birthday;
    this.headerUrl = headerUrl;
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
