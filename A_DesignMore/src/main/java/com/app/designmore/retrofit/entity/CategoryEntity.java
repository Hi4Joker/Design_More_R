package com.app.designmore.retrofit.entity;

/**
 * Created by Joker on 2015/9/24.
 */
public class CategoryEntity implements Cloneable {

  private String catId;
  private String catName;
  private String catThumbUrl;

  public String getCatId() {
    return catId;
  }

  public void setCatId(String catId) {
    this.catId = catId;
  }

  public String getCatName() {
    return catName;
  }

  public void setCatName(String catName) {
    this.catName = catName;
  }

  public String getCatThumbUrl() {
    return catThumbUrl;
  }

  public void setCatThumbUrl(String catThumbUrl) {
    this.catThumbUrl = catThumbUrl;
  }

  public CategoryEntity newInstance() {

    try {
      return (CategoryEntity) super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override protected Object clone() throws CloneNotSupportedException {
    CategoryEntity categoryEntity = (CategoryEntity) super.clone();
    return categoryEntity;
  }

  @Override public String toString() {
    return "CategoryEntity{" +
        "catId='" + catId + '\'' +
        ", catName='" + catName + '\'' +
        ", catThumbUrl='" + catThumbUrl + '\'' +
        '}';
  }
}
