package com.app.designmore.retrofit.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Created by Joker on 2015/9/20.
 */
public class DetailResponse extends BaseResponse {

  /* "result": {
        "goods_id": "52",
        "cat_id": "1",
        "goods_sn": "E900001109",
        "goods_name": "轮播图",
        "goods_name_style": "+",
        "click_count": "8",
        "brand_id": "1",
        "provider_name": "",
        "goods_number": "1",
        "goods_weight": "0.000",
        "market_price": "2398.00",
        "shop_price": "1998.33",
        "promote_price": "0.00",
        "promote_start_date": "0",
        "promote_end_date": "0",
        "warn_number": "1",
        "keywords": "",
        "goods_brief": "",
        "goods_desc": "<p><img src=\"/images/upload/Image/7945_gallery_14303177066072.gif\" width=\"300\" height=\"456\" alt=\"\" /></p>\r\n<p>&nbsp;</p>\r\n<p><img src=\"/images/upload/Image/7945_gallery_14303177039726.gif\" width=\"400\" height=\"571\" alt=\"\" /></p>\r\n<p>&nbsp;</p>\r\n<p>&nbsp;</p>",
        "goods_thumb": "images/201508/thumb_img/51_thumb_G_1440926083065.jpg",
        "goods_img": "images/201508/goods_img/51_G_1440926083389.jpg",
        "original_img": "images/201508/source_img/51_G_1440926083659.gif",
        "is_real": "1",
        "extension_code": "",
        "is_on_sale": "1",
        "is_alone_sale": "1",
        "is_shipping": "0",
        "integral": "19",
        "add_time": "1440926083",
        "sort_order": "100",
        "is_delete": "0",
        "pay_count": "0",
        "is_best": "1",
        "is_new": "1",
        "is_hot": "1",
        "is_promote": "0",
        "bonus_type_id": "0",
        "last_update": "1442186035",
        "goods_type": "1",
        "seller_note": "",
        "give_integral": "0",
        "rank_integral": "0",
        "suppliers_id": "0",
        "collect_count": "0",
        "is_check": null,
        "product_images": [
            {
                "img_id": "39",
                "goods_id": "52",
                "img_url": "images/201509/goods_img/52_P_1441962599613.gif",
                "img_desc": "黄色",
                "thumb_url": "images/201509/thumb_img/52_thumb_P_1441962599713.jpg",
                "img_original": "images/201509/source_img/52_P_1441962599662.gif"
            },
            {
                "img_id": "40",
                "goods_id": "52",
                "img_url": "images/201509/goods_img/52_P_1441962616321.gif",
                "img_desc": "绿色",
                "thumb_url": "images/201509/thumb_img/52_thumb_P_1441962616961.jpg",
                "img_original": "images/201509/source_img/52_P_1441962616182.gif"
            },
            {
                "img_id": "41",
                "goods_id": "52",
                "img_url": "images/201509/goods_img/52_P_1441962643555.gif",
                "img_desc": "红色",
                "thumb_url": "images/201509/thumb_img/52_thumb_P_1441962643503.jpg",
                "img_original": "images/201509/source_img/52_P_1441962643362.gif"
            },
            {
                "img_id": "42",
                "goods_id": "52",
                "img_url": "images/201509/goods_img/52_P_1441962657597.gif",
                "img_desc": "白色",
                "thumb_url": "images/201509/thumb_img/52_thumb_P_1441962657596.jpg",
                "img_original": "images/201509/source_img/52_P_1441962657593.gif"
            }
        ],
        "attr_list": [
            {
                "goods_attr_id": "37",
                "goods_id": "52",
                "attr_id": "1",
                "attr_value": "黄色",
                "attr_price": "0",
                "image": "images/201509/thumb_img/52_thumb_P_1441962599713.jpg"
            },
            {
                "goods_attr_id": "38",
                "goods_id": "52",
                "attr_id": "1",
                "attr_value": "红色",
                "attr_price": "0",
                "image": "images/201509/thumb_img/52_thumb_P_1441962643503.jpg"
            },
            {
                "goods_attr_id": "39",
                "goods_id": "52",
                "attr_id": "1",
                "attr_value": "绿色",
                "attr_price": "0",
                "image": "images/201509/thumb_img/52_thumb_P_1441962616961.jpg"
            },
            {
                "goods_attr_id": "40",
                "goods_id": "52",
                "attr_id": "1",
                "attr_value": "白色",
                "attr_price": "0",
                "image": "images/201509/thumb_img/52_thumb_P_1441962657596.jpg"
            }
        ],
        "zhekou": "8.5"
    },*/

  @Expose @SerializedName("result") private Detail detail;

  public Detail getDetail() {
    return detail;
  }

  public class Detail {

    @Expose @SerializedName("goods_id") public String goodId;
    @Expose @SerializedName("goods_name") public String goodName;
    @Expose @SerializedName("market_price") public String marketPrice;
    @Expose @SerializedName("shop_price") public String shopPrice;
    @Expose @SerializedName("goods_brief") public String goodDes;
    @Expose @SerializedName("original_img") public String goodDesUrl;
    @Expose @SerializedName("goods_number") public String goodRepertory;

    @Expose @SerializedName("product_images") public List<ProductImage> productImages;
    @Expose @SerializedName("attr_list") public List<ProductAttr> productAttrs;

    public class ProductImage {
      @Expose @SerializedName("img_desc") public String thumbDesc;
      @Expose @SerializedName("thumb_url") public String thumbUrl;
    }

    public class ProductAttr {
      @Expose @SerializedName("goods_attr_id") public String attrId;
      @Expose @SerializedName("image") public String attrThumbUrl;
    }
  }
}
