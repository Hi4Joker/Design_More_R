package com.app.designmore.retrofit.request.address;

import com.app.designmore.retrofit.request.BaseRequest;

/**
 * Created by Joker on 2015/9/3.
 */
public class AddressRequest extends BaseRequest {

  /* Action=GetUserByAddress&uid=2*/

  public String uid;

  public AddressRequest(String action, String uid) {
    this.Action = action;
    this.uid = uid;
  }
}
