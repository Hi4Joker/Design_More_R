package com.app.designmore.retrofit.entity;

import com.app.designmore.retrofit.response.DetailResponse;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Created by Joker on 2015/9/20.
 */
public class DetailEntity {

  private String goodId;
  private String goodName;
  private String marketPrice;
  private String shopPrice;
  private String goodDes;
  private String goodDesUrl;
  private String goodRepertory;

  private List<DetailResponse.Detail.ProductImage> productImages;
  private List<DetailResponse.Detail.ProductAttr> productAttrs;

  public DetailEntity(String goodId, String goodName, String marketPrice, String shopPrice,
      String goodDes, String goodDesUrl, String goodRepertory,
      List<DetailResponse.Detail.ProductImage> productImages,
      List<DetailResponse.Detail.ProductAttr> productAttrs) {
    this.goodId = goodId;
    this.goodName = goodName;
    this.marketPrice = marketPrice;
    this.shopPrice = shopPrice;
    this.goodDes = goodDes;
    this.goodDesUrl = goodDesUrl;
    this.goodRepertory = goodRepertory;
    this.productImages = productImages;
    this.productAttrs = productAttrs;
  }

  public String getGoodId() {
    return goodId;
  }

  public String getGoodName() {
    return goodName;
  }

  public String getMarketPrice() {
    return marketPrice;
  }

  public String getShopPrice() {
    return shopPrice;
  }

  public String getGoodDes() {
    return goodDes;
  }

  public String getGoodDesUrl() {
    return goodDesUrl;
  }

  public String getGoodRepertory() {
    return goodRepertory;
  }

  public List<DetailResponse.Detail.ProductImage> getProductImages() {
    return productImages;
  }

  public List<DetailResponse.Detail.ProductAttr> getProductAttrs() {
    return productAttrs;
  }

  @Override public String toString() {
    return "DetailEntity{" +
        "goodId='" + goodId + '\'' +
        ", goodName='" + goodName + '\'' +
        ", marketPrice='" + marketPrice + '\'' +
        ", shopPrice='" + shopPrice + '\'' +
        ", goodDes='" + goodDes + '\'' +
        ", goodDesUrl='" + goodDesUrl + '\'' +
        ", goodRepertory='" + goodRepertory + '\'' +
        ", productImages=" + productImages +
        ", productAttrs=" + productAttrs +
        '}';
  }
}
