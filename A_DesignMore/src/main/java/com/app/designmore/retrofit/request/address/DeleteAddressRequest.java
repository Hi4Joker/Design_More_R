package com.app.designmore.retrofit.request.address;

import com.app.designmore.retrofit.request.BaseRequest;

/**
 * Created by Joker on 2015/9/3.
 */
public class DeleteAddressRequest extends BaseRequest {

  /* Action=DelUserByAddress&address_id=1&uid=2*/

  public String uid;
  public String addressId;

  public DeleteAddressRequest(String action, String addressId, String uid) {
    this.Action = action;
    this.addressId = addressId;
    this.uid = uid;
  }
}
