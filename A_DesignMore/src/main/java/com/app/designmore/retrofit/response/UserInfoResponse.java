package com.app.designmore.retrofit.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Joker on 2015/9/14.
 */
public class UserInfoResponse extends BaseResponse {


  /*{
      "user_name":"linuxlan",用户名
      "nickname":"NULL",昵称
      "sex":"0",性别 0 女 1 男
      "birthday":"0000-00-00",生日
      "question":"",安全问题
      "answer":"",安全问题对应的答案
      "header":"NULL"头像地址 NULL代表是没有头像
      }*/

  @Expose @SerializedName("result") private UserInfo userInfo;

  public UserInfo getUserInfo() {
    return userInfo;
  }

  public class UserInfo {

    @Expose @SerializedName("user_name") public String userName;
    @Expose public String nickname;
    @Expose @SerializedName("sex") public String gender;
    @Expose public String birthday;
    @Expose @SerializedName("header") public String headerUrl;

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

  @Override public String toString() {
    return "UserInfoResponse{" +
        "userInfo=" + userInfo +
        '}';
  }
}
