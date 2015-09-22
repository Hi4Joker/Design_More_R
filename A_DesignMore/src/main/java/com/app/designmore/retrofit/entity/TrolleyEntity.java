package com.app.designmore.retrofit.entity;

/**
 * Created by Joker on 2015/9/5.
 */
public class TrolleyEntity implements Cloneable {

  private String goodId;
  private String goodName;
  private String goodAttr;
  private String goodCount;
  private String goodPrice;
  private String goodThumb;
  private String goodAttrValue;

  public boolean isChecked = false;

  @Override public boolean equals(Object obj) {

    if (obj == null || this.getClass() != obj.getClass() || !this.getGoodId()
        .equals(((TrolleyEntity) obj).getGoodId())) {
      return false;
    }
    return true;
  }

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

  public String getGoodAttr() {
    return goodAttr;
  }

  public void setGoodAttr(String goodAttr) {
    this.goodAttr = goodAttr;
  }

  public String getGoodCount() {
    return goodCount;
  }

  public void setGoodCount(String goodCount) {
    this.goodCount = goodCount;
  }

  public String getGoodPrice() {
    return goodPrice;
  }

  public void setGoodPrice(String goodPrice) {
    this.goodPrice = goodPrice;
  }

  public String getGoodThumb() {
    return goodThumb;
  }

  public void setGoodThumb(String goodThumb) {
    this.goodThumb = goodThumb;
  }

  public String getGoodAttrValue() {
    return goodAttrValue;
  }

  public void setGoodAttrValue(String goodAttrValue) {
    this.goodAttrValue = goodAttrValue;
  }

  public TrolleyEntity newInstance() {

    try {
      return (TrolleyEntity) super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override protected Object clone() throws CloneNotSupportedException {
    TrolleyEntity trolleyEntity = (TrolleyEntity) super.clone();
    return trolleyEntity;
  }
}
