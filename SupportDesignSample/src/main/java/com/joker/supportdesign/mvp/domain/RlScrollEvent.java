package com.joker.supportdesign.mvp.domain;

import android.view.animation.Interpolator;

/**
 * Created by Joker on 2015/7/1.
 */
public class RlScrollEvent {

  private Interpolator interpolator;
  private boolean isShow;

  public RlScrollEvent(boolean isShow) {
    this.isShow = isShow;
  }


  public void setInterpolator(Interpolator interpolator) {
    this.interpolator = interpolator;
  }

  public Interpolator getInterpolator() {
    return interpolator;
  }

  public boolean isShow() {
    return isShow;
  }
}
