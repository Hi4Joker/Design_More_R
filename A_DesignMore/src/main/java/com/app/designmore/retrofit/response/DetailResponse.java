package com.app.designmore.retrofit.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Created by Joker on 2015/9/20.
 */
public class DetailResponse extends BaseResponse {

  /*  "result": {
        "goods_id": "1",
        "cat_id": "2",
        "goods_sn": "ECS000000",
        "goods_name": "测试产品",
        "goods_name_style": "+",
        "click_count": "3",
        "brand_id": "1",
        "provider_name": "",
        "goods_number": "1",
        "goods_weight": "0.100",
        "market_price": "1200.00",
        "shop_price": "1000.00",
        "promote_price": "0.00",
        "promote_start_date": "0",
        "promote_end_date": "0",
        "warn_number": "1",
        "keywords": "家居",
        "goods_brief": "产品",
        "goods_desc": [
            "/images/upload/Image/1080_01.jpg",
            "/images/upload/Image/1080_02.jpg",
            "/images/upload/Image/1080_03.jpg"
        ],
        "goods_thumb": "images/201510/thumb_img/1_thumb_G_1444257594717.jpg",
        "goods_img": "images/201510/source_img/1_G_1444257594356.png",
        "original_img": "images/201510/source_img/1_G_1444257594356.png",
        "is_real": "1",
        "extension_code": "",
        "is_on_sale": "1",
        "is_alone_sale": "1",
        "is_shipping": "0",
        "integral": "10",
        "add_time": "1443573578",
        "sort_order": "100",
        "is_delete": "0",
        "pay_count": "0",
        "is_best": "0",
        "is_new": "1",
        "is_hot": "0",
        "is_promote": "0",
        "bonus_type_id": "0",
        "last_update": "1444257594",
        "goods_type": "1",
        "seller_note": "残品",
        "give_integral": "-1",
        "rank_integral": "-1",
        "suppliers_id": "0",
        "collect_count": "0",
        "is_check": null,
        "product_images": [
            {
                "img_id": "1",
                "goods_id": "1",
                "img_url": "images/201509/goods_img/1_P_1443573578164.jpg",
                "img_desc": "",
                "thumb_url": "images/201509/thumb_img/1_thumb_P_1443573578790.jpg",
                "img_original": "images/201509/source_img/1_P_1443573578857.jpg"
            },
            {
                "img_id": "2",
                "goods_id": "1",
                "img_url": "images/201509/goods_img/1_P_1443574384210.jpg",
                "img_desc": "黄色",
                "thumb_url": "images/201509/thumb_img/1_thumb_P_1443574384796.jpg",
                "img_original": "images/201509/source_img/1_P_1443574384937.jpg"
            },
            {
                "img_id": "3",
                "goods_id": "1",
                "img_url": "images/201509/goods_img/1_P_1443574384605.jpg",
                "img_desc": "绿色",
                "thumb_url": "images/201509/thumb_img/1_thumb_P_1443574384609.jpg",
                "img_original": "images/201509/source_img/1_P_1443574384525.jpg"
            },
            {
                "img_id": "4",
                "goods_id": "1",
                "img_url": "images/201509/goods_img/1_P_1443574384683.jpg",
                "img_desc": "白色",
                "thumb_url": "images/201509/thumb_img/1_thumb_P_1443574384541.jpg",
                "img_original": "images/201509/source_img/1_P_1443574384399.jpg"
            },
            {
                "img_id": "43",
                "goods_id": "1",
                "img_url": "images/201510/goods_img/1_P_1444257594772.png",
                "img_desc": "",
                "thumb_url": "images/201510/thumb_img/1_thumb_P_1444257594547.jpg",
                "img_original": "images/201510/source_img/1_P_1444257594230.png"
            }
        ],
        "attr_list": [
            {
                "goods_attr_id": "1",
                "goods_id": "1",
                "attr_id": "1",
                "attr_value": "黄色",
                "attr_price": "0",
                "image": "images/201509/source_img/1_P_1443574384937.jpg"
            },
            {
                "goods_attr_id": "3",
                "goods_id": "1",
                "attr_id": "1",
                "attr_value": "绿色",
                "attr_price": "0",
                "image": "images/201509/source_img/1_P_1443574384525.jpg"
            },
            {
                "goods_attr_id": "4",
                "goods_id": "1",
                "attr_id": "1",
                "attr_value": "白色",
                "attr_price": "0",
                "image": "images/201509/source_img/1_P_1443574384399.jpg"
            }
        ],
        "zhekou": "8.5"
    },
    },*/

  @Expose @SerializedName("result") private Detail detail;

  public Detail getDetail() {
    return detail;
  }

  public class Detail {

    @Expose @SerializedName("goods_id") public String goodId;
    @Expose @SerializedName("goods_name") public String goodName;
    @Expose @SerializedName("market_price") public String goodMarketPrice;
    @Expose @SerializedName("shop_price") public String goodShopPrice;
    @Expose @SerializedName("goods_brief") public String goodDes;
    @Expose @SerializedName("goods_number") public String goodRepertory;

    /*轮播图*/
    @Expose @SerializedName("lunbo") public List<ProductBanner> productBanners;
    /*详情介绍图*/
    @Expose @SerializedName("goods_desc") public List<String> productImages;
    /*属性图*/
    @Expose @SerializedName("attr_list") public List<ProductAttr> productAttrs;

    public class ProductBanner {
      @Expose @SerializedName("img_original") public String thumbUrl;

      @Override public String toString() {
        return "ProductImage{" +
            "thumbUrl='" + thumbUrl + '\'' +
            '}';
      }
    }

    public class ProductAttr {
      @Expose @SerializedName("goods_attr_id") public String attrId;
      @Expose @SerializedName("attr_value") public String attrValue;
      @Expose @SerializedName("attr_price") public String attrPrice;
      @Expose @SerializedName("image") public String attrThumbUrl;

      @Override public String toString() {
        return "ProductAttr{" +
            "attrId='" + attrId + '\'' +
            ", attrValue='" + attrValue + '\'' +
            ", attrPrice='" + attrPrice + '\'' +
            ", attrThumbUrl='" + attrThumbUrl + '\'' +
            '}';
      }
    }
  }

  @Override public String toString() {
    return "DetailResponse{" +
        "detail=" + detail +
        '}';
  }
}
