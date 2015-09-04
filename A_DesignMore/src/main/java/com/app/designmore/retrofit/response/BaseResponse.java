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

  /*
    "result": [{  }],
    "code": 1,
    "message": ""
  */

  @Expose @SerializedName("code") private int resultCode;
  @Expose public String message;

  public Observable filterWebServiceErrors() {
    if (resultCode == Constants.RESULT_OK) {
      return Observable.just(this);
    } else {
      return Observable.error(new WebServiceException(BaseResponse.this.message));
    }
  }
}
