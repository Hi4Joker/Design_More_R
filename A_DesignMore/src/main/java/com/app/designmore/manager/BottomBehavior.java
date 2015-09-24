package com.app.designmore.manager;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import com.app.designmore.Constants;
import com.app.designmore.utils.DensityUtil;

/**
 * Created by Joker on 2015/9/18.
 */
public class BottomBehavior extends CoordinatorLayout.Behavior<LinearLayout> {

  private static final String TAG = BottomBehavior.class.getCanonicalName();
  private static final int MIN_SCROLL_TO_HIDE = 10;
  private int totalDy;
  private int initialOffset;
  private int accummulatedDy;

  public BottomBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.initialOffset = DensityUtil.getStatusBarHeight(context);
  }

  @Override
  public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, LinearLayout child,
      View directTargetChild, View target, int nestedScrollAxes) {
    return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
  }

  @Override public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, LinearLayout child,
      View target, int dx, int dy, int[] consumed) {

    totalDy += dy;

    if (totalDy < initialOffset) {
      return;
    }

    if (dy > 0) {
      accummulatedDy = accummulatedDy > 0 ? accummulatedDy + dy : dy;
      if (accummulatedDy > MIN_SCROLL_TO_HIDE) {
        BottomBehavior.this.hideView(child);
      }
    } else if (dy < 0) {
      accummulatedDy = accummulatedDy < 0 ? accummulatedDy + dy : dy;
      if (accummulatedDy < -MIN_SCROLL_TO_HIDE) {
        BottomBehavior.this.showView(child);
      }
    }
  }

  private void showView(final View view) {
    BottomBehavior.this.runTranslateAnimation(view, 0, new FastOutSlowInInterpolator(),
        new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationStart(View view) {
            /*do something*/
          }
        });
  }

  private void hideView(final View view) {
    int translateY = BottomBehavior.this.calculateTranslation(view);
    BottomBehavior.this.runTranslateAnimation(view, translateY, new FastOutLinearInInterpolator(),
        new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            /*do something*/
          }
        });
  }

  private int calculateTranslation(View view) {
    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
    int margin = params.topMargin;
    return view.getHeight() + margin;
  }

  private void runTranslateAnimation(View view, int translateY, Interpolator interpolator,
      final ViewPropertyAnimatorListener listener) {

    ViewCompat.animate(view)
        .translationY(translateY)
        .setInterpolator(interpolator)
        .setDuration(Constants.MILLISECONDS_400)
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationStart(View view) {
            if (listener != null) {
              listener.onAnimationStart(view);
            }
          }

          @Override public void onAnimationEnd(View view) {
            if (listener != null) {
              listener.onAnimationEnd(view);
            }
          }
        });
  }
}
