package com.app.designmore.retrofit.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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

  public boolean isChecked = false;

  @Override public boolean equals(Object o) {
    return this.goodId.equals(o);
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
