package com.joker.app.adapter;

/**
 * Created by Joker on 2015/7/17.
 */
public class DetailEntity implements Cloneable {

  private int avatar;
  private String description;

  public DetailEntity() {
  }

  public void setAvatar(int avatar) {
    this.avatar = avatar;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getAvatar() {
    return avatar;
  }

  public String getDescription() {
    return description;
  }

  public DetailEntity newInstance() {

    try {
      return (DetailEntity) super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override protected Object clone() throws CloneNotSupportedException {
    DetailEntity detailEntity = (DetailEntity) super.clone();
    return detailEntity;
  }
}
