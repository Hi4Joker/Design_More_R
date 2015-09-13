package com.app.designmore.retrofit.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Joker on 2015/9/13.
 */
public class LoginResponse extends BaseResponse {

  /* "user_id": "3",  //用户ID
    "email": "",   //用户Email
    "user_name": "",  //用户名字
    "password": "", //密码
    "question": "",//安全问题
    "answer": "",//安全问题答案
    "sex": "0",//性别
    "birthday": "0000-00-00",//生日
    "user_money": "0.00",//用户余额
    "frozen_money": "0.00",//冻结资金
    "pay_points": "0",//消费积分
    "rank_points": "0",//等级积分
    "address_id": "0",//主收货地址信息id
    "reg_time": "0",//注册时间
    "last_login": "0",//上一次登录时间
    "last_time": "0000-00-00 00:00:00",//上一次登陆时间
    "last_ip": "",//上一次登录IP
    "visit_count": "0",//访问次数
    "user_rank": "0",//会员等级id，取值ecs_user_rank
    "is_special": "0",//未知
    "ec_salt": null,//未知
    "salt": "0",//未知
    "parent_id": "0",//推荐人会员id
    "flag": "0",//标记
    "alias": "",//昵称
    "msn": "",//MSN
    "qq": "",//QQ
    "office_phone": "",//电话
    "home_phone": "",//家庭电话
    "mobile_phone": "",//手机号码
    "is_validated": "0",//
    "credit_line": "0.00",//信用额度
    "passwd_question": null,//密码找回问题
    "passwd_answer": null//密码找回答案*/

  @Expose @SerializedName("result") private LoginInfo loginInfo;

  public LoginInfo getLoginInfo() {
    return loginInfo;
  }

  public class LoginInfo {

    @Expose @SerializedName("user_id") public String userId;
    @Expose @SerializedName("address_id") public String addressId;
  }
}
