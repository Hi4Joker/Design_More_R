package com.joker.supportdesign.mvp.domain;

/**
 * Created by Joker on 2015/6/29.
 */
public class Animal implements Cloneable {

  private String url;
  private String name;

  public String getUrl() {
    return url;
  }

  public String getName() {
    return name;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Animal newInstance() {

    try {
      return (Animal) super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override protected Object clone() throws CloneNotSupportedException {
    Animal find = (Animal) super.clone();
    return find;
  }
}
