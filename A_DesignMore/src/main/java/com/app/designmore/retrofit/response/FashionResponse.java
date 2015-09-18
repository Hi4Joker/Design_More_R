package com.app.designmore.retrofit.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Created by Joker on 2015/9/18.
 */
public class FashionResponse extends BaseResponse {


  /*"goods_id": "9",//产品id
     "goods_name": "设计猫 台湾抽取式垃圾桶 ", //产品名字
     "goods_img": "",//产品图片
     "zhekou":"8.5折起"
     */

  @Expose @SerializedName("result") private List<Fashion> fashions;

  public List<Fashion> getFashions() {
    return fashions;
  }

  public class Fashion {

    @Expose @SerializedName("goods_id") public String goodId;
    @Expose @SerializedName("goods_name") public String goodName;
    @Expose @SerializedName("goods_img") public String goodThumb;
    @Expose @SerializedName("zhekou") public String discount;

    @Override public String toString() {
      return "Fashion{" +
          "goodId='" + goodId + '\'' +
          ", goodName='" + goodName + '\'' +
          ", goodThumb='" + goodThumb + '\'' +
          ", discount='" + discount + '\'' +
          '}';
    }
  }

  @Override public String toString() {
    return "FashionResponse{" +
        "fashions=" + fashions +
        '}';
  }
}
