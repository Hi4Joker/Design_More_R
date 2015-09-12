package com.app.designmore.retrofit.entity;

/**
 * Created by Joker on 2015/9/13.
 */
public class HelpEntity implements Cloneable {

  private String title;
  private String content;

  public void setContent(String content) {
    this.content = content;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public String getContent() {
    return content;
  }

  public HelpEntity newInstance() {

    try {
      return (HelpEntity) super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override protected Object clone() throws CloneNotSupportedException {
    HelpEntity helpEntity = (HelpEntity) super.clone();
    return helpEntity;
  }

  @Override public String toString() {
    return "HelpEntity{" +
        "content='" + content + '\'' +
        ", title='" + title + '\'' +
        '}';
  }
}
