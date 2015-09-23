package com.app.designmore.retrofit.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Joker on 2015/9/5.
 */
public class TrolleyEntity implements Cloneable, Parcelable {

  private String recId;/*唯一标识*/
  private String goodId;
  private String goodName;
  private String goodAttrId;
  private String goodCount;
  private String goodPrice;
  private String goodThumb;
  private String goodAttrValue;

  /*private String goodMaxCount;

  /*标记是否选择状态*/
  public boolean isChecked = false;

  public TrolleyEntity() {
  }

  @Override public boolean equals(Object obj) {

    if (obj == null || this.getClass() != obj.getClass() || !this.getRecId()
        .equals(((TrolleyEntity) obj).getRecId())) {
      return false;
    }
    return true;
  }

  public String getRecId() {
    return recId;
  }

  public void setRecId(String recId) {
    this.recId = recId;
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

  public String getGoodAttrId() {
    return goodAttrId;
  }

  public void setGoodAttrId(String goodAttrId) {
    this.goodAttrId = goodAttrId;
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

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.recId);
    dest.writeString(this.goodId);
    dest.writeString(this.goodName);
    dest.writeString(this.goodAttrId);
    dest.writeString(this.goodCount);
    dest.writeString(this.goodPrice);
    dest.writeString(this.goodThumb);
    dest.writeString(this.goodAttrValue);
    dest.writeByte(isChecked ? (byte) 1 : (byte) 0);
  }

  protected TrolleyEntity(Parcel in) {
    this.recId = in.readString();
    this.goodId = in.readString();
    this.goodName = in.readString();
    this.goodAttrId = in.readString();
    this.goodCount = in.readString();
    this.goodPrice = in.readString();
    this.goodThumb = in.readString();
    this.goodAttrValue = in.readString();
    this.isChecked = in.readByte() != 0;
  }

  public static final Creator<TrolleyEntity> CREATOR = new Creator<TrolleyEntity>() {
    public TrolleyEntity createFromParcel(Parcel source) {
      return new TrolleyEntity(source);
    }

    public TrolleyEntity[] newArray(int size) {
      return new TrolleyEntity[size];
    }
  };
}
