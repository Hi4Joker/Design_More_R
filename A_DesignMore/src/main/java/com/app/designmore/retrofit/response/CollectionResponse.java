package com.app.designmore.retrofit.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Created by Joker on 2015/9/4.
 */
public class CollectionResponse extends BaseResponse {

  /* "rec_id": "1",
      "user_id": "1", //用户id
      "goods_id": "1", //产品ID
      "add_time": "1440953101", //添加时间
      "is_attention": "0",
      "goods_info": { //请看产品详情
          "goods_id": "1",  //商品id
          "cat_id": "0", //商品所属商品分类id ，取值 category 的 cat_id
          "goods_sn": "ECS000000", //商品的唯一货号
          "goods_name": "设餐垫 木质加厚隔热防滑厨", //商品的名称
          "goods_name_style": "+", //商品名称显示的样式；包括颜色和字体样式；格式如#ff00ff+strong
          "click_count": "0", //商品点击数
          "brand_id": "0",  //品牌id ，取值于 brand 的 brand_id
          "provider_name": "", //供货人的名称，程序还没实现该功能
          "goods_number": "7", //商品库存数量
          "goods_weight": "0.000", //商品的重量，以千克为单位
          "market_price": "60.00", //市场售价
          "shop_price": "60.00", //本店售价
          "promote_price": "0.00", //促销价格
          "promote_start_date": "0",  //促销价格开始日期
          "promote_end_date": "0", //促销价格结束日期
          "warn_number": "1", //商品报警数量
          "keywords": "", //商品关键字，放在商品页的关键字中，为搜索引擎收录用
          "goods_brief": "", //商品的简短描述
          "goods_desc": "2015-08-25 17:30:38 ", //商品的详细描述
          "goods_thumb": "", //商品在前台显示的微缩图片，如在分类筛选时显示的小图片
          "goods_img": "", //商品的实际大小图片，如进入该商品页时介绍商品属性所显示的大图片
          "original_img": "", //应该是上传的商品的原始图片
          "is_real": "1", //是否是实物，1 ，是； 0 ，否；比如虚拟卡就为 0 ，不是实物
          "extension_code": "", //品的扩展属性，比如像虚拟卡
          "is_on_sale": "1", //该商品是否开放销售，1 ，是； 0 ，否
          "is_alone_sale": "0", //是否能单独销售，1 ，是； 0 ，否；
          "is_shipping": "0",
          "integral": "0", //使用的积分的数量，取用户使用的积分，商品可用积分，用户拥有积分中最小者
          "add_time": "1440470965", //订单生成时间
          "sort_order": "100", //显示顺序，位数越大越靠后
          "is_delete": "0",
          "is_best": "0",
          "is_new": "0",
          "is_hot": "0",
          "is_promote": "0",
          "bonus_type_id": "0",
          "last_update": "1440470965",
          "goods_type": "0",
          "seller_note": "",
          "give_integral": "-1",
          "rank_integral": "-1",
          "suppliers_id": null,
          "is_check": null
          }*/

  @Expose @SerializedName("result") public List<Collect> collections;

  public List<Collect> getCollections() {
    return collections;
  }

  public class Collect {

    //@Expose @SerializedName("goods_id") public String goodId;
    @Expose @SerializedName("goods_info") public GoodInfo goodInfo;

    public GoodInfo getGoodInfo() {
      return goodInfo;
    }

    public class GoodInfo {

      @Expose @SerializedName("goods_id") public String goodId;
      @Expose @SerializedName("goods_name") public String goodName;
      @Expose @SerializedName("shop_price") public String goodPrice;
      @Expose @SerializedName("goods_thumb") public String goodThumb;
      @Expose @SerializedName("goods_img") public String goodImage;

      @Override public String toString() {
        return "GoodInfo{" +
            "goodId='" + goodId + '\'' +
            ", goodName='" + goodName + '\'' +
            ", goodPrice='" + goodPrice + '\'' +
            ", goodThumb='" + goodThumb + '\'' +
            ", goodImage='" + goodImage + '\'' +
            '}';
      }
    }

    @Override public String toString() {
      return "Collect{" +
          "goodInfo=" + goodInfo +
          '}';
    }
  }

  @Override public String toString() {
    return "CollectionResponse{" +
        "collections=" + collections +
        '}';
  }
}
