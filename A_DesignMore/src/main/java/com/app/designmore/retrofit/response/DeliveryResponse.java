package com.app.designmore.retrofit.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Created by Joker on 2015/10/8.
 */
public class DeliveryResponse extends BaseResponse {


  /*{
      "shipping_id": "1",
      "shipping_code": "sto_express",
      "shipping_name": "申通快递",
      "shipping_desc": "江、浙、沪地区首重为15元/KG，其他地区18元/KG， 续重均为5-6元/KG， 云南地区为8元",
      "insure": "0",
      "support_cod": "0",
      "enabled": "1",
      "shipping_print": "",
      "print_bg": "/images/receipt/dly_sto_express.jpg",
      "config_lable": "t_shop_address,网店-地址,235,48,131,152,b_shop_address||,||t_shop_name,网店-名称,237,26,131,200,b_shop_name||,||t_shop_tel,网店-联系电话,96,36,144,257,b_shop_tel||,||t_customer_post,收件人-邮编,86,23,578,268,b_customer_post||,||t_customer_address,收件人-详细地址,232,49,434,149,b_customer_address||,||t_customer_name,收件人-姓名,151,27,449,231,b_customer_name||,||t_customer_tel,收件人-电话,90,32,452,261,b_customer_tel||,||",
      "print_model": "2",
      "shipping_order": "0",
      "area_list": [
          "福建",
          "甘肃",
          "广东",
          "广西",
          "贵州",
          "海南",
          "河北",
          "安庆",
          "蚌埠",
          "巢湖",
          "池州",
          "滁州",
          "阜阳",
          "淮北",
          "淮南",
          "东城区",
          "西城区",
          "海淀区",
          "朝阳区",
          "崇文区",
          "宣武区",
          "丰台区",
          "石景山区",
          "房山区",
          "门头沟区",
          "通州区",
          "顺义区",
          "昌平区",
          "怀柔区"
      ],
      "area_price": [
          [
              {
                  "name": "item_fee",
                  "value": "0"
              },
              {
                  "name": "base_fee",
                  "value": "1"
              },
              {
                  "name": "step_fee",
                  "value": "0"
              },
              {
                  "name": "free_money",
                  "value": "100"
              },
              {
                  "name": "fee_compute_mode",
                  "value": "by_weight"
              }
          ]
      ],
      "base_fee": "1"
  },*/

  @Expose @SerializedName("result") public List<Delivery> deliveries;

  public List<Delivery> getDeliveries() {
    return deliveries;
  }

  public class Delivery {

    @Expose @SerializedName("shipping_id") public String DeliveryId;
    @Expose @SerializedName("shipping_name") public String DeliveryName;
    @Expose @SerializedName("base_fee") public String DeliveryFee;

    @Override public String toString() {
      return "DeliveryResponse{" +
          "DeliveryId='" + DeliveryId + '\'' +
          ", DeliveryName='" + DeliveryName + '\'' +
          ", DeliveryFee='" + DeliveryFee + '\'' +
          '}';
    }
  }
}
