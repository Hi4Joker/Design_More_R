package com.app.designmore.retrofit.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Joker on 2015/9/4.
 */
public class CollectionEntity implements Cloneable {

  private String goodId;
  private String goodName;
  private String goodPrice;
  private String goodThumb;

  private String collectionId;

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

  public String getCollectionId() {
    return collectionId;
  }

  public void setCollectionId(String collectionId) {
    this.collectionId = collectionId;
  }

  public CollectionEntity newInstance() {

    try {
      return (CollectionEntity) super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override protected Object clone() throws CloneNotSupportedException {
    CollectionEntity collectionEntity = (CollectionEntity) super.clone();
    return collectionEntity;
  }

  @Override public String toString() {
    return "CollectionEntity{" +
        "goodId='" + goodId + '\'' +
        ", goodName='" + goodName + '\'' +
        ", goodPrice='" + goodPrice + '\'' +
        ", goodThumbUrl='" + goodThumb + '\'' +
        '}';
  }
}
