package com.app.designmore.retrofit.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joker on 2015/9/5.
 */
public class TrolleyResponse extends BaseResponse {

  /*
      "rec_id": "2", //自增id
      "user_id": "1", //用户id
      "session_id": "1", //sessionID
      "goods_id": "1", //产品ID
      "goods_sn": "ECS000000", //产品编号
      "product_id": "1",//产品ID
      "goods_name": "设计猫天鼎 创意中国风文武官补子纹杯垫餐垫 木质加厚隔热防滑厨",//产品名称
      "market_price": "60.00", //市场价格
      "goods_price": "60.00", //产品价格
      "goods_number": "1", //产品数量
      "goods_attr": "", //购买商品时选择的属性
      "is_real": "1", //是否是实物
      "extension_code": "", //商品的扩展属性，比如像虚拟卡，取值ecs_goods
      "parent_id": "0", //父商品id，如果有值则是代表的物品的配件
      "rec_type": "0", //购物车类型 0普通；1 团购；2拍卖；3夺宝奇兵
      "is_gift": "0", //是否是赠品， 0否，是，参见优惠活动的id，取值于ecs_favourable_activity
      "is_shipping": "0", //未知
      "can_handsel": "0",//未知
      "goods_attr_id": "NULL" //商品属性id*/

  @Expose @SerializedName("result") private List<Trolley> addressList = new ArrayList<>();

  public List<Trolley> getTrolleyList() {
    return addressList;
  }

  public class Trolley {

    @Expose @SerializedName("goods_id") public String goodId;
    @Expose @SerializedName("goods_name") public String goodName;
    @Expose @SerializedName("goods_attr") public String goodAttr;
    @Expose @SerializedName("goods_number") public String goodCount;
    @Expose @SerializedName("goods_price") public String goodPrice;
    //public String goodThumb;

    @Override public String toString() {
      return "Trolley{" +
          "goodId='" + goodId + '\'' +
          ", goodName='" + goodName + '\'' +
          ", goodAttr='" + goodAttr + '\'' +
          ", goodCount='" + goodCount + '\'' +
          ", goodPrice='" + goodPrice + '\'' +
          '}';
    }
  }

  @Override public String toString() {
    return "TrolleyResponse{" +
        "addressList=" + addressList +
        '}';
  }
}
