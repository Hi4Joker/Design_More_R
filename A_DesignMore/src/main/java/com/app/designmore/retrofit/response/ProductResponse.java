package com.app.designmore.retrofit.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Created by Joker on 2015/9/21.
 */
public class ProductResponse extends BaseResponse {

  /*   {
            "goods_id": "9",
            "cat_id": "5",
            "goods_sn": "ECS000009",
            "goods_name": "卡拉手提包中号 CARMINA CAMPUS+",
            "goods_name_style": "+",
            "click_count": "4",
            "brand_id": "1",
            "provider_name": "",
            "goods_number": "98",
            "goods_weight": "0.000",
            "market_price": "1198.80",
            "shop_price": "999.00",
            "promote_price": "0.00",
            "promote_start_date": "0",
            "promote_end_date": "0",
            "warn_number": "1",
            "keywords": "",
            "goods_brief": "",
            "goods_desc": "<p>&nbsp;</p>\r\n<p><img src=\"/images/upload/Image/1080_01.jpg\" alt=\"\" /></p>\r\n<p>&nbsp;</p>\r\n<p><img src=\"/images/upload/Image/1080_02.jpg\" alt=\"\" /></p>\r\n<p><img src=\"/images/upload/Image/1080_03.jpg\" alt=\"\" /></p>",
            "goods_thumb": "images/201509/source_img/9_G_1443592087782.jpg",
            "goods_img": "images/201509/source_img/9_G_1443592087782.jpg",
            "original_img": "images/201509/source_img/9_G_1443592087782.jpg",
            "is_real": "1",
            "extension_code": "",
            "is_on_sale": "1",
            "is_alone_sale": "1",
            "is_shipping": "0",
            "integral": "0",
            "add_time": "1443592087",
            "sort_order": "100",
            "is_delete": "0",
            "pay_count": "0",
            "is_best": "0",
            "is_new": "0",
            "is_hot": "1",
            "is_promote": "0",
            "bonus_type_id": "0",
            "last_update": "1444344804",
            "goods_type": "1",
            "seller_note": "",
            "give_integral": "-1",
            "rank_integral": "-1",
            "suppliers_id": "0",
            "collect_count": "0",
            "is_check": null,
            "zhekou": "8.5折起",
            "goods_price": "999.00"
        }
  }*/

  @Expose @SerializedName("result") private List<Product> products;

  public List<Product> getProducts() {
    return products;
  }

  public class Product {

    @Expose @SerializedName("goods_id") public String goodId;
    @Expose @SerializedName("shop_price") public String goodPrice;
    @Expose @SerializedName("goods_name") public String goodName;
    @Expose @SerializedName("original_img") public String goodThumb;

    @Override public String toString() {
      return "Product{" +
          "goodId='" + goodId + '\'' +
          ", goodPrice='" + goodPrice + '\'' +
          ", goodName='" + goodName + '\'' +
          ", goodThumbUrl='" + goodThumb + '\'' +
          '}';
    }
  }

  @Override public String toString() {
    return "ProductEntity{" +
        "products=" + products +
        '}';
  }
}
