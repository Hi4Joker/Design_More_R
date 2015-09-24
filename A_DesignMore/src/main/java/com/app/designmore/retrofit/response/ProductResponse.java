package com.app.designmore.retrofit.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Created by Joker on 2015/9/21.
 */
public class ProductResponse extends BaseResponse {

  /*  "goods_id": "60",
      "cat_id": "1",
      "goods_sn": "213214212",
      "goods_name": "aqua手表",
      "goods_name_style": "+",
      "click_count": "19",
      "brand_id": "1",
      "provider_name": "",
      "goods_number": "5",
      "goods_weight": "0.220",
      "market_price": "698.00",
      "shop_price": "499.00",
      "promote_price": "0.00",
      "promote_start_date": "0",
      "promote_end_date": "0",
      "warn_number": "2",
      "keywords": "手表 潮流",
      "goods_brief": "",
      "goods_desc": "描述"
      "goods_thumb": "images/201508/thumb_img/50_thumb_G_1440628873364.jpg",
      "goods_img": "images/201508/goods_img/50_G_1440628873546.jpg",
      "original_img": "images/201508/source_img/50_G_1440",
      "goods_name_style": "+",
      "click_count": "13",
      "brand_id": "1",
      "provider_name": "",
      "goods_number": "5",
      "goods_weight": "0.220",
      "market_price": "698.00",
      "shop_price": "499.00",
      "promote_price": "0.00",
      "promote_start_date": "0",
      "promote_end_date": "0",
      "warn_number": "2",
      "keywords": "手表 潮流",
      "goods_brief": "",
      "goods_desc": "<p>&nbsp;测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述测试详细描述</p>",
      "goods_thumb": "images/201508/thumb_img/50_thumb_G_1440628873364.jpg",
      "goods_img": "images/201508/goods_img/50_G_1440628873546.jpg",
      "original_img": "images/201508/source_img/50_G_1440628873956.jpg",
      "is_real": "1",
      "extension_code": "",
      "is_on_sale": "1",
      "is_alone_sale": "1",
      "is_shipping": "0",
      "integral": "4",
      "add_time": "1440628873",
      "sort_order": "100",
      "is_delete": "0",
      "pay_count": "0",
      "is_best": "1",
      "is_new": "1",
      "is_hot": "1",
      "is_promote": "0",
      "bonus_type_id": "0",
      "last_update": "1442186038",
      "goods_type": "1",
      "seller_note": "",
      "give_integral": "-1",
      "rank_integral": "-1",
      "suppliers_id": "0",
      "collect_count": "0",
      "is_check": null,
      "zhekou": "8.5折起"
  }*/

  @Expose @SerializedName("result") private List<Product> product;

  public List<Product> getProducts() {
    return product;
  }

  public class Product {

    @Expose @SerializedName("goods_id") public String goodId;
    @Expose @SerializedName("shop_price") public String goodPrice;
    @Expose @SerializedName("goods_desc") public String goodDes;
    @Expose @SerializedName("goods_thumb") public String goodThumb;

    @Override public String toString() {
      return "Product{" +
          "goodId='" + goodId + '\'' +
          ", goodPrice='" + goodPrice + '\'' +
          ", goodDes='" + goodDes + '\'' +
          ", goodThumbUrl='" + goodThumb + '\'' +
          '}';
    }
  }

  @Override public String toString() {
    return "ProductEntity{" +
        "product=" + product +
        '}';
  }
}
