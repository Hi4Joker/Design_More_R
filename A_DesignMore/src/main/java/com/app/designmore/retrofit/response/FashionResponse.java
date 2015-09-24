package com.app.designmore.retrofit.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Created by Joker on 2015/9/18.
 */
public class FashionResponse extends BaseResponse {


   /*result": [
        {
            "goods_id": "52",
            "goods_name": "轮播图",
            "goods_img": "images/201508/source_img/51_G_1440926083659.gif",
            "goods_sn": "E900001109",
            "original_img": "images/201508/source_img/51_G_1440926083659.gif",
            "zhekou": "8.5折起"
        },
        {
            "goods_id": "59",
            "goods_name": "123445",
            "goods_img": "images/201509/source_img/59_G_1441759271860.png",
            "goods_sn": "E9000012",
            "original_img": "images/201509/source_img/59_G_1441759271860.png",
            "zhekou": "8.5折起"
        }
    ]*/

  @Expose @SerializedName("result") private List<Fashion> fashions;

  public List<Fashion> getFashions() {
    return fashions;
  }

  public class Fashion {

    @Expose @SerializedName("goods_id") public String goodId;
    @Expose @SerializedName("goods_name") public String goodName;
    @Expose @SerializedName("zhekou") public String goodDiscount;
    @Expose @SerializedName("original_img") public String goodThumbUrl;

    @Override public String toString() {
      return "Fashion{" +
          "goodId='" + goodId + '\'' +
          ", goodName='" + goodName + '\'' +
          ", goodDiscount='" + goodDiscount + '\'' +
          ", goodThumbUrl='" + goodThumbUrl + '\'' +
          '}';
    }
  }

  @Override public String toString() {
    return "FashionResponse{" +
        "fashions=" + fashions +
        '}';
  }
}
