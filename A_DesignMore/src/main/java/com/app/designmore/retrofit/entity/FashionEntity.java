package com.app.designmore.retrofit.entity;

/**
 * Created by Joker on 2015/9/18.
 */
public class FashionEntity implements Cloneable {

  private String goodId;
  private String goodName;
  private String goodDiscount;
  private String goodThumbUrl;

  public String getGoodId() {
    return goodId;
  }

  public void setGoodId(String goodId) {
    this.goodId = goodId;
  }

  public String getGoodName() {
    return goodName;
  }

  public void setGoodName(String goodName) {
    this.goodName = goodName;
  }

  public String getGoodDiscount() {
    return goodDiscount;
  }

  public void setGoodDiscount(String goodDiscount) {
    this.goodDiscount = goodDiscount;
  }

  public String getGoodThumbUrl() {
    return goodThumbUrl;
  }

  public void setGoodThumbUrl(String goodThumbUrl) {
    this.goodThumbUrl = goodThumbUrl;
  }

  public FashionEntity newInstance() {

    try {
      return (FashionEntity) super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override protected Object clone() throws CloneNotSupportedException {
    FashionEntity fashionEntity = (FashionEntity) super.clone();
    return fashionEntity;
  }

  @Override public String toString() {
    return "FashionEntity{" +
        "goodId='" + goodId + '\'' +
        ", goodName='" + goodName + '\'' +
        ", goodDiscount='" + goodDiscount + '\'' +
        ", goodThumbUrl='" + goodThumbUrl + '\'' +
        '}';
  }
}
