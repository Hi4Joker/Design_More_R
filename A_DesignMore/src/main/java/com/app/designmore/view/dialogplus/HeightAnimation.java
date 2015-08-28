package com.app.designmore.view.dialogplus;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * @author Orhan Obut
 */
class HeightAnimation extends Animation {

  protected final int originalHeight;
  protected final View view;
  protected float perValue;

  public HeightAnimation(View view, int fromHeight, int toHeight) {
    this.view = view;
    this.originalHeight = fromHeight;
    this.perValue = (toHeight - fromHeight);
  }

  @Override
  protected void applyTransformation(float interpolatedTime, Transformation t) {
    view.getLayoutParams().height = (int) (originalHeight + perValue * interpolatedTime);
    view.requestLayout();
  }

  @Override
  public boolean willChangeBounds() {
    return true;
  }
}
