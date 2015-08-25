package com.joker.app.view.circularProgress;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.widget.TextView;

class MorphingAnimation {

  /*Morphing duration*/
  public static final int DURATION_NORMAL = 400;
  public static final int DURATION_INSTANT = 1;
  private static final String TAG = MorphingAnimation.class.getSimpleName();

  private AnimatorListenerAdapter listenerAdapter;

  private int mDuration;

  private int mFromWidth;
  private int mToWidth;

  private int mFromColor;
  private int mToColor;

  private int mFromStrokeColor;
  private int mToStrokeColor;

  private float mFromCornerRadius;
  private float mToCornerRadius;

  private float mPadding;

  private TextView mView;
  private StrokeGradientDrawable mDrawable;

  public MorphingAnimation(TextView viewGroup, StrokeGradientDrawable drawable) {
    mView = viewGroup;
    mDrawable = drawable;
  }

  public void start() {
    ValueAnimator widthAnimation = ValueAnimator.ofInt(mFromWidth, mToWidth);
    final GradientDrawable gradientDrawable = mDrawable.getGradientDrawable();
    widthAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        Integer value = (Integer) animation.getAnimatedValue();
        int leftOffset;
        int rightOffset;
        int padding;

        if (mFromWidth > mToWidth) {
          leftOffset = (mFromWidth - value) / 2;
          rightOffset = mFromWidth - leftOffset;
          padding = (int) (mPadding * animation.getAnimatedFraction());
        } else {
          leftOffset = (mToWidth - value) / 2;
          rightOffset = mToWidth - leftOffset;
          padding = (int) (mPadding - mPadding * animation.getAnimatedFraction());
        }

        gradientDrawable.setBounds(leftOffset + padding, padding, rightOffset - padding,
            mView.getHeight() - padding);
      }
    });

    ObjectAnimator backgroundColorAnimation =
        ObjectAnimator.ofInt(gradientDrawable, "color", mFromColor, mToColor);
    backgroundColorAnimation.setEvaluator(new ArgbEvaluator());

    ObjectAnimator strokeColorAnimation =
        ObjectAnimator.ofInt(mDrawable, "strokeColor", mFromStrokeColor, mToStrokeColor);
    strokeColorAnimation.setEvaluator(new ArgbEvaluator());

    ObjectAnimator cornerAnimation =
        ObjectAnimator.ofFloat(gradientDrawable, "cornerRadius", mFromCornerRadius,
            mToCornerRadius);

    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.setDuration(mDuration);
    animatorSet.playTogether(widthAnimation, cornerAnimation, backgroundColorAnimation,
        strokeColorAnimation);
    animatorSet.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        if (listenerAdapter != null) {
          listenerAdapter.onAnimationEnd(animation);
        }
      }
    });
    animatorSet.start();
  }

  public void setDuration(int duration) {
    mDuration = duration;
  }

  public void setListener(AnimatorListenerAdapter listener) {
    listenerAdapter = listener;
  }

  public void setFromWidth(int fromWidth) {
    mFromWidth = fromWidth;
  }

  public void setToWidth(int toWidth) {
    mToWidth = toWidth;
  }

  public void setFromColor(int fromColor) {
    mFromColor = fromColor;
  }

  public void setToColor(int toColor) {
    mToColor = toColor;
  }

  public void setFromStrokeColor(int fromStrokeColor) {
    mFromStrokeColor = fromStrokeColor;
  }

  public void setToStrokeColor(int toStrokeColor) {
    mToStrokeColor = toStrokeColor;
  }

  public void setFromCornerRadius(float fromCornerRadius) {
    mFromCornerRadius = fromCornerRadius;
  }

  public void setToCornerRadius(float toCornerRadius) {
    mToCornerRadius = toCornerRadius;
  }

  public void setPadding(float padding) {
    mPadding = padding;
  }
}