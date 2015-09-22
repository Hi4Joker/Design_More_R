package com.app.designmore.retrofit.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joker on 2015/9/5.
 */
public class TrolleyResponse extends BaseResponse {

  /*  "rec_id": "76",
      "user_id": "37",
      "session_id": "37",
      "goods_id": "52",
      "goods_sn": "E900001109",
      "product_id": "52",
      "goods_name": "轮播图",
      "market_price": "2398.00",
      "goods_price": "1998.33",
      "goods_number": "1",
      "goods_attr": "37",
      "is_real": "1",
      "extension_code": "",
      "parent_id": "0",
      "rec_type": "0",
      "is_gift": "0",
      "is_shipping": "0",
      "can_handsel": "0",
      "goods_attr_id": "37",
      "goods_img": "images/201508/goods_img/51_G_1440926083389.jpg",
      "goods_attr_str": "黄色"
    */

  @Expose @SerializedName("result") private List<Trolley> addressList = new ArrayList<>();

  public List<Trolley> getTrolleyList() {
    return addressList;
  }

  public class Trolley {

    @Expose @SerializedName("goods_id") public String goodId;
    @Expose @SerializedName("goods_name") public String goodName;
    @Expose @SerializedName("goods_attr") public String goodAttrId;
    @Expose @SerializedName("goods_number") public String goodCount;
    @Expose @SerializedName("goods_price") public String goodPrice;
    @Expose @SerializedName("goods_img") public String goodThumb;
    @Expose @SerializedName("goods_attr_str") public String goodAttrValue;

    @Override public String toString() {
      return "Trolley{" +
          "goodAttrId='" + goodAttrId + '\'' +
          ", goodId='" + goodId + '\'' +
          ", goodName='" + goodName + '\'' +
          ", goodCount='" + goodCount + '\'' +
          ", goodPrice='" + goodPrice + '\'' +
          ", goodThumb='" + goodThumb + '\'' +
          ", goodAttrValue='" + goodAttrValue + '\'' +
          '}';
    }
  }

  @Override public String toString() {
    return "TrolleyResponse{" +
        "addressList=" + addressList +
        '}';
  }
}
