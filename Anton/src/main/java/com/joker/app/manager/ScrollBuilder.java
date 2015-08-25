package com.joker.app.manager;

import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.animation.Interpolator;
import java.util.HashMap;

/**
 * Created by Joker on 2015/7/22.
 */
public class ScrollBuilder {

  Scroller.Direction direction;
  Interpolator showInterpolator;
  Interpolator hideInterpolator;
  int offset;
  HashMap<View, Scroller.Direction> viewsToHide = new HashMap<>();

  public ScrollBuilder addView(View view, Scroller.Direction direction) {

    this.viewsToHide.put(view, direction);
    return ScrollBuilder.this;
  }

  private ScrollBuilder setDirection(Scroller.Direction direction) {
    this.direction = direction;
    return ScrollBuilder.this;
  }

  public ScrollBuilder setShowInterpolator(Interpolator interpolator) {
    this.showInterpolator = interpolator;
    return ScrollBuilder.this;
  }

  public ScrollBuilder setHideInterpolator(Interpolator interpolator) {
    this.hideInterpolator = interpolator;
    return ScrollBuilder.this;
  }

  public ScrollBuilder initOffset(int offset) {
    this.offset = offset;
    return ScrollBuilder.this;
  }

  public Scroller build() {
    return new Scroller(this);
  }
}
