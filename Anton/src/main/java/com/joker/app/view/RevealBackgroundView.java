package com.joker.app.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

import com.joker.app.R;

/**
 * Created by Miroslaw Stanek on 18.01.15.
 *
 * fixed by Joker
 */
public class RevealBackgroundView extends View {

  private static final String TAG = RevealBackgroundView.class.getSimpleName();
  public static final int STATE_NOT_STARTED = 0;
  public static final int STATE_FILL_STARTED = 1;
  public static final int STATE_FINISHED = 2;

  private Interpolator INTERPOLATOR = new AccelerateInterpolator();
  public static final int FILL_TIME = 500;
  private static final int ALPHA_TIME = FILL_TIME * 2 / 3;

  private volatile int state = STATE_NOT_STARTED;

  private Paint fillPaint;
  private int currentAngle;

  private int startLocationX;
  private int startLocationY;

  private OnStateChangeListener onStateChangeListener;
  private int revealColor;
  private int startAlpha;
  private int endAlpha;

  private float currentAlpha;

  private Property<RevealBackgroundView, Integer> revealProperty =
      new Property<RevealBackgroundView, Integer>(Integer.class, "angle") {
        @Override public Integer get(RevealBackgroundView revealBackgroundView) {
          return revealBackgroundView.getCurrentAngle();
        }

        @Override public void set(RevealBackgroundView revealBackgroundView, Integer value) {
          revealBackgroundView.setCurrentAngle(value);
        }
      };

  private Property<RevealBackgroundView, Float> alphaProperty =
      new Property<RevealBackgroundView, Float>(Float.class, "alpha") {
        @Override public Float get(RevealBackgroundView revealBackgroundView) {
          return revealBackgroundView.getCurrentAlpha();
        }

        @Override public void set(RevealBackgroundView revealBackgroundView, Float value) {
          revealBackgroundView.setCurrentAlpha(value);
        }
      };

  public RevealBackgroundView(Context context) {
    super(context);
    init(context, null);
  }

  public RevealBackgroundView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public RevealBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attributeSet) {

    RevealBackgroundView.this.initAttributes(context, attributeSet);

    fillPaint = new Paint();
    fillPaint.setStyle(Paint.Style.FILL);
    fillPaint.setAntiAlias(true);
    fillPaint.setColor(revealColor);
  }

  private void initAttributes(Context context, AttributeSet attributeSet) {

    TypedArray typedArray =
        context.obtainStyledAttributes(attributeSet, R.styleable.RevealBackgroundAttr, 0, 0);

    if (typedArray == null) {
      return;
    }

    try {
      revealColor = typedArray.getColor(R.styleable.RevealBackgroundAttr_reveal_color,
          getResources().getColor(android.R.color.white));
      startAlpha = typedArray.getInt(R.styleable.RevealBackgroundAttr_start_alpha,
          getResources().getInteger(R.integer.reveal_start_alpha));
      endAlpha = typedArray.getInt(R.styleable.RevealBackgroundAttr_end_alpha,
          getResources().getInteger(R.integer.reveal_end_alpha));
    } catch (Exception ignored) {
    } finally {
      typedArray.recycle();
    }
  }

  public void setRevealColor(int color) {
    fillPaint.setColor(color);
  }

  public void startFromLocation(int[] tapLocationOnScreen) {

    RevealBackgroundView.this.changeState(STATE_FILL_STARTED);
    startLocationX = tapLocationOnScreen[0];
    startLocationY = tapLocationOnScreen[1];
    currentAngle = tapLocationOnScreen[2];

    ObjectAnimator revealAnimator =
        ObjectAnimator.ofInt(this, revealProperty, currentAngle, getWidth() + getHeight())
            .setDuration(FILL_TIME);
    revealAnimator.setInterpolator(INTERPOLATOR);
    revealAnimator.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        RevealBackgroundView.this.changeState(STATE_FINISHED);
      }
    });

    ObjectAnimator alphaAnimator =
        ObjectAnimator.ofFloat(this, alphaProperty, startAlpha, endAlpha).setDuration(ALPHA_TIME);
    alphaAnimator.setInterpolator(INTERPOLATOR);
    alphaAnimator.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        if (onStateChangeListener != null) {
          onStateChangeListener.onAlphaFinish();
        }
      }
    });

    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playTogether(revealAnimator, alphaAnimator);
    animatorSet.start();
  }

  public void setToFinishedFrame() {
    RevealBackgroundView.this.changeState(STATE_FINISHED);
    invalidate();
  }

  @Override protected void onDraw(Canvas canvas) {

    if (state == STATE_FINISHED) {
      canvas.drawRect(0, 0, getWidth(), getHeight(), fillPaint);
    } else if (state == STATE_FILL_STARTED) {
      canvas.drawCircle(startLocationX, startLocationY, currentAngle, fillPaint);
    }
  }

  private void changeState(int state) {
    if (this.state == state) {
      return;
    }

    this.state = state;
    if (onStateChangeListener != null) {
      onStateChangeListener.onStateChange(state);
    }
  }

  private void setCurrentAngle(int angle) {
    this.currentAngle = angle;
    invalidate();
  }

  private int getCurrentAngle() {
    return currentAngle;
  }

  public Float getCurrentAlpha() {
    return currentAlpha;
  }

  public void setCurrentAlpha(float currentAlpha) {
    this.currentAlpha = currentAlpha;
    fillPaint.setAlpha((int) currentAlpha);
  }

  public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
    this.onStateChangeListener = onStateChangeListener;
  }

  public interface OnStateChangeListener {
    void onStateChange(int state);

    void onAlphaFinish();
  }
}
