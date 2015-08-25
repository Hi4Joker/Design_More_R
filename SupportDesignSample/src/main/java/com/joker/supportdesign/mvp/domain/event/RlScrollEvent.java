package com.joker.supportdesign.mvp.domain.event;

import android.view.animation.Interpolator;

/**
 * Created by Joker on 2015/7/1.
 */
public class RlScrollEvent {

  private boolean isShow;

  public RlScrollEvent(boolean isShow) {
    this.isShow = isShow;
  }

  public boolean isShow() {
    return isShow;
  }
}
