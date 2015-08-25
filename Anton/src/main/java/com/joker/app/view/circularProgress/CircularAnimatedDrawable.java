package com.joker.app.view.circularProgress;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.Property;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

class CircularAnimatedDrawable extends Drawable implements Animatable {

  private static final Interpolator ANGLE_INTERPOLATOR = new LinearInterpolator();
  private static final Interpolator SWEEP_INTERPOLATOR = new DecelerateInterpolator();

  private static final int ANGLE_ANIMATOR_DURATION = 600;
  private static final int SWEEP_ANIMATOR_DURATION = 800;
  public static final int MIN_SWEEP_ANGLE = 30;
  public static final float PROGRESS_PADDING = 40;
  private static final String TAG = CircularAnimatedDrawable.class.getSimpleName();

  private ObjectAnimator mObjectAnimatorSweep;
  private ObjectAnimator mObjectAnimatorAngle;

  private final RectF dstRectF = new RectF();

  private boolean mModeAppearing;
  private volatile boolean mRunning;
  private Paint arcPaint;
  private float mCurrentGlobalAngleOffset;
  private float mCurrentGlobalAngle;
  private float mCurrentSweepAngle;
  private float mBorderWidth;

  /*自定义属性动画Property*/
  private Property<CircularAnimatedDrawable, Float> mAngleProperty =
      new Property<CircularAnimatedDrawable, Float>(Float.class, "angle") {
        @Override public Float get(CircularAnimatedDrawable circularAnimatedDrawable) {
          return circularAnimatedDrawable.getCurrentGlobalAngle();
        }

        @Override public void set(CircularAnimatedDrawable circularAnimatedDrawable, Float value) {
          circularAnimatedDrawable.setCurrentGlobalAngle(value);
        }
      };

  private Property<CircularAnimatedDrawable, Float> mSweepProperty =
      new Property<CircularAnimatedDrawable, Float>(Float.class, "sweep") {
        @Override public Float get(CircularAnimatedDrawable circularAnimatedDrawable) {
          return circularAnimatedDrawable.getCurrentSweepAngle();
        }

        @Override public void set(CircularAnimatedDrawable circularAnimatedDrawable, Float value) {
          circularAnimatedDrawable.setCurrentSweepAngle(value);
        }
      };

  public CircularAnimatedDrawable(int indicatorColor, float borderWidth) {

    this.mBorderWidth = borderWidth;
    CircularAnimatedDrawable.this.setupAnimations();

    arcPaint = new Paint();
    arcPaint.setAntiAlias(true);
    arcPaint.setStyle(Paint.Style.STROKE);
    arcPaint.setStrokeWidth(borderWidth / 2);
    arcPaint.setColor(indicatorColor);
  }

  public CircularAnimatedDrawable(Shader shader, float borderWidth) {

    this.mBorderWidth = borderWidth;
    CircularAnimatedDrawable.this.setupAnimations();

    arcPaint = new Paint();
    arcPaint.setAntiAlias(true);
    arcPaint.setStyle(Paint.Style.STROKE);
    arcPaint.setStrokeWidth(borderWidth);
    arcPaint.setShader(shader);
  }

  /**
   * 自定义 Drawable 四个必须实现的方法
   */
  @Override public void draw(Canvas canvas) {

    CircularAnimatedDrawable.this.drawArc(canvas);
  }

  @Override public void setAlpha(int alpha) {

  }

  @Override public void setColorFilter(ColorFilter cf) {

  }

  private void drawArc(Canvas canvas) {

    float startAngle = mCurrentGlobalAngle - mCurrentGlobalAngleOffset;
    float sweepAngle = mCurrentSweepAngle;
    if (!mModeAppearing) {
      startAngle = startAngle + sweepAngle;
      sweepAngle = 360 - sweepAngle - MIN_SWEEP_ANGLE;
    } else {
      sweepAngle += MIN_SWEEP_ANGLE;
    }

    canvas.drawArc(dstRectF, startAngle, sweepAngle, false, arcPaint);
  }

  @Override public int getOpacity() {
    return PixelFormat.TRANSPARENT;
  }

  private void toggleAppearingMode() {
    mModeAppearing = !mModeAppearing;
    if (mModeAppearing) {
      mCurrentGlobalAngleOffset = (mCurrentGlobalAngleOffset + MIN_SWEEP_ANGLE * 2) % 360;
    }
  }

  @Override protected void onBoundsChange(Rect bounds) {
    super.onBoundsChange(bounds);

    dstRectF.left = bounds.left + mBorderWidth + PROGRESS_PADDING;
    dstRectF.right = bounds.right - mBorderWidth - PROGRESS_PADDING;
    dstRectF.top = bounds.top + mBorderWidth + PROGRESS_PADDING;
    dstRectF.bottom = bounds.bottom - mBorderWidth - PROGRESS_PADDING;
  }

  private void setupAnimations() {

    mObjectAnimatorAngle = ObjectAnimator.ofFloat(this, mAngleProperty, 0f, 360f);
    mObjectAnimatorAngle.setInterpolator(ANGLE_INTERPOLATOR);
    mObjectAnimatorAngle.setDuration(ANGLE_ANIMATOR_DURATION);
    mObjectAnimatorAngle.setRepeatMode(ValueAnimator.RESTART);
    mObjectAnimatorAngle.setRepeatCount(ValueAnimator.INFINITE);

    mObjectAnimatorSweep =
        ObjectAnimator.ofFloat(this, mSweepProperty, 0f, 360f - MIN_SWEEP_ANGLE * 2);
    mObjectAnimatorSweep.setInterpolator(SWEEP_INTERPOLATOR);
    mObjectAnimatorSweep.setDuration(SWEEP_ANIMATOR_DURATION);
    mObjectAnimatorSweep.setRepeatMode(ValueAnimator.RESTART);
    mObjectAnimatorSweep.setRepeatCount(ValueAnimator.INFINITE);
    mObjectAnimatorSweep.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationRepeat(Animator animation) {
        super.onAnimationRepeat(animation);
        CircularAnimatedDrawable.this.toggleAppearingMode();
      }
    });
  }

  @Override public void start() {
    if (isRunning()) {
      return;
    }
    mRunning = true;
    mObjectAnimatorAngle.start();
    mObjectAnimatorSweep.start();
    invalidateSelf();
  }

  @Override public void stop() {
    if (!isRunning()) {
      return;
    }
    mRunning = false;
    mObjectAnimatorAngle.cancel();
    mObjectAnimatorSweep.cancel();
    invalidateSelf();
  }

  @Override public boolean isRunning() {
    return mRunning;
  }

  public void setCurrentGlobalAngle(float currentGlobalAngle) {
    mCurrentGlobalAngle = currentGlobalAngle;
    invalidateSelf();
  }

  public float getCurrentGlobalAngle() {
    return mCurrentGlobalAngle;
  }

  public void setCurrentSweepAngle(float currentSweepAngle) {
    mCurrentSweepAngle = currentSweepAngle;
    invalidateSelf();
  }

  public float getCurrentSweepAngle() {
    return mCurrentSweepAngle;
  }
}
