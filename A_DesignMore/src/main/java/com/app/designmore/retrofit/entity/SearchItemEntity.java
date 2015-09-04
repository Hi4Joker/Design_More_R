package com.app.designmore.retrofit.entity;

/**
 * Created by Joker on 2015/9/4.
 */
public class SearchItemEntity implements Cloneable {

  private String text;

  public void setText(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public SearchItemEntity newInstance() {

    try {
      return (SearchItemEntity) super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override protected Object clone() throws CloneNotSupportedException {
    SearchItemEntity searchItemEntity = (SearchItemEntity) super.clone();
    return searchItemEntity;
  }

  @Override public String toString() {
    return "SearchItemEntity{" +
        "text='" + text + '\'' +
        '}';
  }
}
