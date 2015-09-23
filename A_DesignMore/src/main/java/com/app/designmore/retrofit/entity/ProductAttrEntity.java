package com.app.designmore.retrofit.entity;

/**
 * Created by Joker on 2015/9/20.
 */
public class ProductAttrEntity implements Cloneable {

  private String attrId;
  private String attrValue;
  private String attrThumbUrl;
  private String attrPrice;
  private boolean isChecked;

  public ProductAttrEntity() {

  }

  public ProductAttrEntity(String attrId, String attrValue, String attrPrice, String attrThumbUrl,
      boolean isChecked) {
    this.attrId = attrId;
    this.attrValue = attrValue;
    this.attrThumbUrl = attrThumbUrl;
    this.attrPrice = attrPrice;
    this.isChecked = isChecked;
  }

  @Override public boolean equals(Object obj) {

    if (obj == null || this.getClass() != obj.getClass() || !this.getAttrId()
        .equals(((ProductAttrEntity) obj).getAttrId())) {
      return false;
    }
    return true;
  }

  public String getAttrId() {
    return attrId;
  }

  public void setAttrId(String attrId) {
    this.attrId = attrId;
  }

  public String getAttrValue() {
    return attrValue;
  }

  public void setAttrValue(String attrValue) {
    this.attrValue = attrValue;
  }

  public String getAttrThumbUrl() {
    return attrThumbUrl;
  }

  public void setAttrThumbUrl(String attrThumbUrl) {
    this.attrThumbUrl = attrThumbUrl;
  }

  public String getAttrPrice() {
    return attrPrice;
  }

  public void setAttrPrice(String attrPrice) {
    this.attrPrice = attrPrice;
  }

  public boolean isChecked() {
    return isChecked;
  }

  public void setIsChecked(boolean isChecked) {
    this.isChecked = isChecked;
  }

  public ProductAttrEntity newInstance() {

    try {
      return (ProductAttrEntity) super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override protected Object clone() throws CloneNotSupportedException {
    ProductAttrEntity productAttrEntity = (ProductAttrEntity) super.clone();
    return productAttrEntity;
  }

  @Override public String toString() {
    return "ProductAttrEntity{" +
        "attrId='" + attrId + '\'' +
        ", attrValue='" + attrValue + '\'' +
        ", attrThumbUrl='" + attrThumbUrl + '\'' +
        ", attrPrice='" + attrPrice + '\'' +
        ", isChecked=" + isChecked +
        '}';
  }
}
