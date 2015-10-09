package com.app.designmore.retrofit.entity;

import com.app.designmore.retrofit.response.DetailResponse;
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
  private String goodRepertory;

  private List<DetailResponse.Detail.ProductBanner> productBanners;
  private List<String> productImages;
  private List<DetailResponse.Detail.ProductAttr> productAttrs;

  public DetailEntity(String goodId, String goodName, String marketPrice, String shopPrice,
      String goodDes, String goodRepertory, List<DetailResponse.Detail.ProductBanner> productBanners,
      List<String> productImages, List<DetailResponse.Detail.ProductAttr> productAttrs) {
    this.goodId = goodId;
    this.goodName = goodName;
    this.marketPrice = marketPrice;
    this.shopPrice = shopPrice;
    this.goodDes = goodDes;
    this.goodRepertory = goodRepertory;
    this.productBanners = productBanners;
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

  public List<String> getProductImages() {
    return productImages;
  }

  public String getGoodRepertory() {
    return goodRepertory;
  }

  public List<DetailResponse.Detail.ProductBanner> getProductBanners() {
    return productBanners;
  }

  public List<DetailResponse.Detail.ProductAttr> getProductAttrs() {
    return productAttrs;
  }

  @Override public String toString() {
    return "DetailEntity{" +
        "goodId='" + goodId + '\'' +
        ", goodName='" + goodName + '\'' +
        ", goodMarketPrice='" + marketPrice + '\'' +
        ", goodShopPrice='" + shopPrice + '\'' +
        ", goodName='" + goodDes + '\'' +
        ", productImages='" + productImages + '\'' +
        ", goodRepertory='" + goodRepertory + '\'' +
        ", productBanners=" + productBanners +
        ", productAttrs=" + productAttrs +
        '}';
  }
}
