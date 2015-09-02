package com.app.designmore.event;

import com.app.designmore.retrofit.entity.Address;

/**
 * Created by Joker on 2015/9/3.
 */
public class EditorAddressEvent extends Address {

  public EditorAddressEvent(String addressId, String userName, String province, String city,
      String address, String mobile, String zipcode, boolean isChecked) {
    this.addressId = addressId;
    this.userName = userName;
    this.province = province;
    this.city = city;
    this.address = address;
    this.mobile = mobile;
    this.zipcode = zipcode;
    this.isChecked = isChecked;
  }
}
