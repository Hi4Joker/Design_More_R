package com.app.designmore.retrofit.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/9/1.
 */
public class AddressResponse extends BaseResponse {

  /* "address_id": "1", //地址ID 修改/删除 地址的时候需要传递过来
      "address_name": "",//名称
      "user_id": "2",//用户ID
      "consignee": "Eric",//收货人
      "email": "NULL",//邮箱
      "country": "0",//国家
      "province": "0",//地区
      "city": "0",//城市
      "district": "0",// 街道
      "address": "北京什么什么地方",//详细地址
      "zipcode": "1000111",//邮政编码
      "tel": "18622816323",//电话
      "mobile": "18622816323",//电话
      "sign_building": "NULL",//
      "best_time": "0"//
*/

  @Expose @SerializedName("result") public List<Address> addressList = new ArrayList<>();

  public List<Address> getAddressList() {
    return addressList;
  }

  public class Address {

    @Expose @SerializedName("address_id") public String addressId;
    @Expose @SerializedName("consignee") public String userName;
    @Expose public String province;
    @Expose public String city;
    @Expose public String address;
    @Expose public String mobile;
    @Expose public String zipcode;

    public boolean isChecked;

    @Override public String toString() {
      return "Address{" +
          ", addressId='" + addressId + '\'' +
          ", consignee='" + userName + '\'' +
          ", province='" + province + '\'' +
          ", city='" + city + '\'' +
          ", address='" + address + '\'' +
          ", mobile='" + mobile + '\'' +
          ", zipcode='" + zipcode + '\'' +
          '}';
    }
  }

  @Override public String toString() {
    return "AddressResponse{" +
        "addressList=" + addressList +
        '}';
  }
}
