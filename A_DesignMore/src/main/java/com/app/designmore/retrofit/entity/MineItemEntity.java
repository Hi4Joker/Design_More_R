package com.app.designmore.retrofit.entity;

/**
 * Created by Joker on 2015/10/12.
 */
public class MineItemEntity {

  private String title;
  private int drawable;

  public MineItemEntity(int drawable, String title) {
    this.drawable = drawable;
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public int getDrawable() {
    return drawable;
  }
}
