package com.joker.supportdesign.mvp.domain.event;

/**
 * Created by Joker on 2015/7/6.
 */
public class RlScrollYEvent {

  private int Y = -1;

  public RlScrollYEvent(int y) {
    this.Y = y;
  }

  public int getY() {
    return Y;
  }
}
