package com.app.designmore.retrofit.request.address;

import com.app.designmore.retrofit.request.BaseRequest;

/**
 * Created by Joker on 2015/9/3.
 */
public class AddAddressRequest extends BaseRequest {

  /*Action=AddUserByAddress
    &consignee=Eric //收货人
    &mobile=18622816323 //手机号码
    &zipcode=1000111  //邮政编码
    &province=%E5%8C%97%E4%BA%AC //省市
    &city=%E5%8C%97%E4%BA%AC //城市
    &address= //详细地址*/

  public String consignee;
  public String mobile;
  public String zipcode;
  public String province;
  public String city;
  public String address;

  public AddAddressRequest(String action, String consignee, String mobile, String zipcode,
      String province, String city, String address) {

    this.Action = action;
    this.consignee = consignee;
    this.mobile = mobile;
    this.zipcode = zipcode;
    this.province = province;
    this.city = city;
    this.address = address;
  }
}
