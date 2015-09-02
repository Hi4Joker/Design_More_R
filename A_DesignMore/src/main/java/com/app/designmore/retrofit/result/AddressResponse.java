package com.app.designmore.retrofit.result;

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

  public List<Address> setAddressList() {
    return addressList;
  }

  public List<Address> getAddressList() {
    return addressList;
  }

  public class Address {

    @Expose @SerializedName("user_id") public String userId;
    @Expose @SerializedName("address_id") public String addressId;
    @Expose @SerializedName("address_name") public String addressName;
    @Expose public String consignee;
    @Expose public String country;
    @Expose public String province;
    @Expose public String city;
    @Expose public String district;
    @Expose public String address;
    @Expose public String zipcode;
    @Expose public String mobile;

    public boolean isChecked;

    @Override public String toString() {
      return "Address{" +
          "userId='" + userId + '\'' +
          ", addressId='" + addressId + '\'' +
          ", addressName='" + addressName + '\'' +
          ", consignee='" + consignee + '\'' +
          ", country='" + country + '\'' +
          ", province='" + province + '\'' +
          ", city='" + city + '\'' +
          ", district='" + district + '\'' +
          ", address='" + address + '\'' +
          ", zipcode='" + zipcode + '\'' +
          ", mobile='" + mobile + '\'' +
          '}';
    }
  }

  @Override public String toString() {

   /* for (Address address : addressList) {

    }*/

    return "AddressResponse{" +
        "addressList=" + addressList +
        '}';
  }
}
