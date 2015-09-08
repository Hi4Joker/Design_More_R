package com.app.designmore.event;

import com.app.designmore.retrofit.entity.AddressEntity;

/**
 * Created by Joker on 2015/9/3.
 */
public class EditorAddressEvent extends AddressEntity {

  public EditorAddressEvent(String addressId, String userName, String province, String city,
      String address, String mobile, String zipcode, String isDefault) {
    this.addressId = addressId;
    this.userName = userName;
    this.province = province;
    this.city = city;
    this.address = address;
    this.mobile = mobile;
    this.zipcode = zipcode;
    this.isDefault = isDefault;
  }
}
