package com.joker.supportdesign.ui;

import android.animation.AnimatorSet;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import android.view.animation.CycleInterpolator;
import android.widget.RelativeLayout;

import com.joker.supportdesign.R;

import java.util.Random;

/**
 * Created by Joker on 26/6/15.
 */
public class SweetLayout extends RelativeLayout {

  private static final String TAG = SweetLayout.class.getSimpleName();

  private View sweetView = null;

  private final Random random = new Random();

  private int duration = 6 * 1000;//移动距离所需时间
  private int mFadeInOutMs = 666;//图片淡出时间

  /*各种参数*/
  private float maxScaleFactor = 1.8f;
  private float minScaleFactor = 0.9f;
  private float speedFactor = 1.0f;

  private AnimatorSet animatorSet;

  @SuppressLint("HandlerLeak") private Handler handler = new Handler() {
    @Override public void handleMessage(Message msg) {
      super.handleMessage(msg);
      if (animatorSet == null) {
        SweetLayout.this.startKenBurnsAnimation();
      } else if (animatorSet != null) {
        animatorSet.start();
      }
    }
  };

 /* private Runnable runnable = new Runnable() {
    @Override public void run() {
      SweetLayout.this.startKenBurnsAnimation();
    }
  };*/

  public SweetLayout(Context context) {
    this(context, null);
    initAttributes(context, null);
  }

  public SweetLayout(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
    initAttributes(context, attrs);
  }

  public SweetLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initAttributes(context, attrs);
  }

  private void initAttributes(Context context, AttributeSet attributeSet) {

    TypedArray typedArray =
        context.obtainStyledAttributes(attributeSet, R.styleable.SweetLayoutAttr, 0, 0);

    if (typedArray == null) {
      return;
    }
    try {
      maxScaleFactor = typedArray.getFloat(R.styleable.SweetLayoutAttr_maxScale, maxScaleFactor);
      minScaleFactor = typedArray.getFloat(R.styleable.SweetLayoutAttr_minScale, minScaleFactor);
      speedFactor = typedArray.getFloat(R.styleable.SweetLayoutAttr_speed, speedFactor);
    } catch (Exception ignored) {
    } finally {
      typedArray.recycle();
    }
  }

  private void startKenBurnsAnimation() {

    int w = sweetView.getWidth() / 4;
    int h = sweetView.getHeight() / 4;

    float fromScale = calculateScale();
    float toScale = calculateScale();
    float fromTranslationX = calculateTranslation(sweetView.getWidth(), fromScale);
    float fromTranslationY = calculateTranslation(sweetView.getHeight(), fromScale);
    float toTranslationX = calculateTranslation(sweetView.getWidth(), toScale);
    float toTranslationY = calculateTranslation(sweetView.getHeight(), toScale);

    /*缩放动画*/
    PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.9f, 1.0f, 0.9f);
    PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.9f, 1.0f, 0.9f);
    ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(sweetView, scaleX, scaleY);
    scaleAnimator.setRepeatCount(ValueAnimator.INFINITE);
    scaleAnimator.setRepeatMode(ValueAnimator.REVERSE);


    /*位移动画*/
    /*PropertyValuesHolder translateX =
        PropertyValuesHolder.ofFloat("translationX", 0f, -30f, -50f, -30f, 0f, 30f, 50f, 30f);*/
    /*PropertyValuesHolder translateY =
        PropertyValuesHolder.ofFloat("translationY", 0f, -30f, -50f, -30f, 0f, 30f, 50f, 30f);*/
    PropertyValuesHolder translateX =
        PropertyValuesHolder.ofKeyframe(View.TRANSLATION_X, Keyframe.ofFloat(0f, -10f),
            Keyframe.ofFloat(.1f, -20f), Keyframe.ofFloat(.2f, -30f), Keyframe.ofFloat(.3f, -20f),
            Keyframe.ofFloat(.4f, -10f), Keyframe.ofFloat(.5f, 10f), Keyframe.ofFloat(.6f, 20f),
            Keyframe.ofFloat(.7f, 30f), Keyframe.ofFloat(.8f, 20f), Keyframe.ofFloat(.9f, 10f),
            Keyframe.ofFloat(1f, 0f));
    PropertyValuesHolder translateY =
        PropertyValuesHolder.ofKeyframe(View.TRANSLATION_Y, Keyframe.ofFloat(0f, -10f),
            Keyframe.ofFloat(.1f, 0f), Keyframe.ofFloat(.2f, 10f), Keyframe.ofFloat(.3f, 0f),
            Keyframe.ofFloat(.4f, -10f), Keyframe.ofFloat(.5f, 0f), Keyframe.ofFloat(.6f, 10f),
            Keyframe.ofFloat(.7f, 0f), Keyframe.ofFloat(.8f, -10f), Keyframe.ofFloat(.9f, 0f),
            Keyframe.ofFloat(1f, 10f));

    ObjectAnimator translateAnimator =
        ObjectAnimator.ofPropertyValuesHolder(sweetView, translateX, translateY);
    translateAnimator.setRepeatCount(ValueAnimator.INFINITE);
    translateAnimator.setRepeatMode(ValueAnimator.REVERSE);


    /*动画叠加*/
    animatorSet = new AnimatorSet();
    animatorSet.play(scaleAnimator).with(translateAnimator);
    animatorSet.setDuration(duration);
    animatorSet.start();


    /*SweetLayout.this.animate(duration, fromScale, toScale, fromTranslationX, fromTranslationY,
        toTranslationX, toTranslationY);*/
  }

  private void animate(long duration, float fromScale, float toScale, float fromTranslationX,
      float fromTranslationY, float toTranslationX, float toTranslationY) {
    sweetView.setScaleX(fromScale);
    sweetView.setScaleY(fromScale);
    sweetView.setTranslationX(fromTranslationX);
    sweetView.setTranslationY(fromTranslationY);
    sweetView.animate()
        .translationX(toTranslationX)
        .translationY(toTranslationY)
        .scaleX(toScale)
        .scaleY(toScale)
        .setInterpolator(new CycleInterpolator(10))
        .setDuration(duration);
  }

  private void reAnimate(long duration, float fromScale, float toScale, float fromTranslationX,
      float fromTranslationY, float toTranslationX, float toTranslationY) {
    sweetView.setScaleX(fromScale);
    sweetView.setScaleY(fromScale);
    sweetView.setTranslationX(fromTranslationX);
    sweetView.setTranslationY(fromTranslationY);
    sweetView.animate()
        .translationX(toTranslationX)
        .translationY(toTranslationY)
        .scaleX(toScale)
        .scaleY(toScale)
        .setDuration(duration);
  }

  /**
   * 计算缩放比例
   */

  private float calculateScale() {
    if (maxScaleFactor < minScaleFactor) {
      throw new IllegalArgumentException("maxScaleFactor must bigger than minScaleFactor");
    }

    return minScaleFactor + random.nextFloat() * (maxScaleFactor - minScaleFactor);
  }

  private float calculateTranslation(int value, float ratio) {
    return value * (ratio - 1.0f) * (random.nextFloat() - 0.5f);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    //Log.e(TAG, "onAttachedToWindow");
    handler.sendEmptyMessage(0);
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    //Log.e(TAG, "onDetachedFromWindow");
    if (animatorSet != null && animatorSet.isRunning()) {
      animatorSet.cancel();
      sweetView = null;
      animatorSet = null;
    }
    handler.removeCallbacksAndMessages(null);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();

    sweetView = getChildAt(0);
    if (sweetView == null) {
      throw new IllegalArgumentException("no sweetView");
    }
  }

  /*正方形哒，square shape*/
  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, widthMeasureSpec);
  }
}
