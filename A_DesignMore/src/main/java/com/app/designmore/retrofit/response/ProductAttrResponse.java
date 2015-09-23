package com.app.designmore.retrofit.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Created by Joker on 2015/9/24.
 */
public class ProductAttrResponse extends BaseResponse {


  /*result": [
    {
      "goods_attr_id": "9", 产品选项ID
      "goods_id": "59",产品id
      "attr_id": "1",选项ID
      "attr_value": "黄色", 数据
      "attr_price": "0",颜色对应的价格
      "image": "images/201509/thumb_img/59_thumb_P_1441757378768.jpg" 图片
    }
  ],*/

  @Expose @SerializedName("result") private List<Attr> attrs;

  public List<Attr> getAttrs() {
    return attrs;
  }

  public class Attr {

    @Expose @SerializedName("goods_attr_id") public String goodsAttrId;
    @Expose @SerializedName("attr_value") public String goodsAttrValue;
    @Expose @SerializedName("attr_price") public String goodsAttrPrice;
    @Expose @SerializedName("image") public String goodsAttrThumb;

    @Override public String toString() {
      return "Attr{" +
          ", goodsAttrId='" + goodsAttrId + '\'' +
          ", goodsAttrValue='" + goodsAttrValue + '\'' +
          ", goodsAttrPrice='" + goodsAttrPrice + '\'' +
          ", goodsAttrThumb='" + goodsAttrThumb + '\'' +
          '}';
    }
  }

  @Override public String toString() {
    return "ProductAttrResponse{" +
        "attrs=" + attrs +
        '}';
  }
}
