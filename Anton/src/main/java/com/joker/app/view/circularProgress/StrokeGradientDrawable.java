package com.joker.app.view.circularProgress;

import android.graphics.drawable.GradientDrawable;

public class StrokeGradientDrawable {

  private int mStrokeWidth;
  private int mStrokeColor;

  private GradientDrawable mGradientDrawable;

  public StrokeGradientDrawable(GradientDrawable drawable) {
    mGradientDrawable = drawable;
  }

  public void initStrokeAndColor(int strokeWidth, int strokeColor) {

    this.mStrokeWidth = strokeWidth;
    this.mStrokeColor = strokeColor;
    mGradientDrawable.setStroke(strokeWidth, strokeColor);
  }

  /**
   * 属性动画
   */
  public void setStrokeWidth(int strokeWidth) {
    this.mStrokeWidth = strokeWidth;
    mGradientDrawable.setStroke(strokeWidth, mStrokeColor);
  }

  public void setStrokeColor(int strokeColor) {
    this.mStrokeColor = strokeColor;
    mGradientDrawable.setStroke(mStrokeWidth, strokeColor);
  }

  public GradientDrawable getGradientDrawable() {
    return mGradientDrawable;
  }
}
