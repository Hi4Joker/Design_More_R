package com.app.designmore.retrofit.entity;

/**
 * Created by Joker on 2015/9/20.
 */
public class ProductAttrEntity {

  private String attrId;
  private String url;
  private boolean isChecked;

  public ProductAttrEntity(String attrId, String url, boolean isChecked) {
    this.attrId = attrId;
    this.url = url;
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

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public boolean isChecked() {
    return isChecked;
  }

  public void setIsChecked(boolean isChecked) {
    this.isChecked = isChecked;
  }

  @Override public String toString() {
    return "ProductAttrEntity{" +
        "attrId='" + attrId + '\'' +
        ", url='" + url + '\'' +
        ", isChecked=" + isChecked +
        '}';
  }
}
