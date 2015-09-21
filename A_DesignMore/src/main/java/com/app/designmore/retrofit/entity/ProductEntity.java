package com.app.designmore.retrofit.entity;

/**
 * Created by Joker on 2015/9/21.
 */
public class ProductEntity implements Cloneable {

  private String goodId;
  private String goodPrice;
  private String goodDes;
  private String goodThumbUrl;

  public String getGoodId() {
    return goodId;
  }

  public void setGoodId(String goodId) {
    this.goodId = goodId;
  }

  public String getGoodPrice() {
    return goodPrice;
  }

  public void setGoodPrice(String goodPrice) {
    this.goodPrice = goodPrice;
  }

  public String getGoodDes() {
    return goodDes;
  }

  public void setGoodDes(String goodDes) {
    this.goodDes = goodDes;
  }

  public String getGoodThumbUrl() {
    return goodThumbUrl;
  }

  public void setGoodThumbUrl(String goodThumbUrl) {
    this.goodThumbUrl = goodThumbUrl;
  }

  public ProductEntity newInstance() {

    try {
      return (ProductEntity) super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override protected Object clone() throws CloneNotSupportedException {
    ProductEntity productEntity = (ProductEntity) super.clone();
    return productEntity;
  }

  @Override public String toString() {
    return "ProductEntity{" +
        "goodId='" + goodId + '\'' +
        ", goodPrice='" + goodPrice + '\'' +
        ", goodDes='" + goodDes + '\'' +
        ", goodThumbUrl='" + goodThumbUrl + '\'' +
        '}';
  }
}
