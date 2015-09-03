package com.app.designmore.retrofit.response;

import com.app.designmore.Constants;
import com.app.designmore.exception.WebServiceException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import rx.Observable;

/**
 * Created by Joker on 2015/9/1.
 */
public class BaseResponse {

  /*"Result": false,
  "MessageCode": 102,
  "MessageString": "无效调用",
  "InsertID": 0,
  "Charset": ""*/

  @Expose @SerializedName("MessageCode") private int resultCode;
  @Expose public String MessageString;
  @Expose public long InsertID;

  public Observable filterWebServiceErrors() {
    if (resultCode == Constants.RESULT_OK) {
      return Observable.just(this);
    } else {
      return Observable.error(new WebServiceException(BaseResponse.this.MessageString));
    }
  }
}
