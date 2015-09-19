package com.app.designmore.manager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Path;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import com.app.designmore.Constants;
import com.app.designmore.revealLib.widget.RevealFrameLayout;
import com.app.designmore.utils.DensityUtil;

/**
 * Created by Joker on 2015/9/18.
 */
public class HeaderBehavior extends CoordinatorLayout.Behavior<View> {

  private static final String TAG = HeaderBehavior.class.getCanonicalName();
  private static final int MIN_SCROLL_TO_HIDE = 10;
  private int totalDy;
  private int initialOffset;
  private int accummulatedDy;
  //private int mDySinceDirectionChange = 0;

  private CoordinatorLayout coordinatorLayout;
  private RevealFrameLayout revealFrameLayout;

  public HeaderBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.initialOffset = DensityUtil.getStatusBarHeight(context);
  }

  @Override public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {

    this.coordinatorLayout = parent;
    //this.revealFrameLayout = (RevealFrameLayout) coordinatorLayout.getChildAt(0);

    return dependency instanceof RecyclerView;
  }

  @Override public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child,
      View directTargetChild, View target, int nestedScrollAxes) {
    return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
  }

  @Override
  public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, View child, View target,
      int dx, int dy, int[] consumed) {

    //Log.e(TAG, dy + "");

    /*if (dy > 0 && sinceDirectionChange < 0 || dy < 0 && sinceDirectionChange > 0) {
            child.animate().cancel();
            sinceDirectionChange = 0;
        }
        sinceDirectionChange += dy;
        if (sinceDirectionChange > child.getHeight() && child.getVisibility() == View.VISIBLE) {
            hide(child);
        } else if (sinceDirectionChange < 0 && child.getVisibility() == View.GONE) {
            show(child);
        }*/

    totalDy += dy;

    if (totalDy < initialOffset) {
      return;
    }

    if (dy > 0) {
      accummulatedDy = accummulatedDy > 0 ? accummulatedDy + dy : dy;
      if (accummulatedDy > MIN_SCROLL_TO_HIDE) {
        HeaderBehavior.this.hideView(child);
      }
    } else if (dy < 0) {
      accummulatedDy = accummulatedDy < 0 ? accummulatedDy + dy : dy;
      if (accummulatedDy < -MIN_SCROLL_TO_HIDE) {
        HeaderBehavior.this.showView(child);
      }
    }
  }

  @Override
  public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {

    Log.e(TAG, dependency.getTranslationY() + "");

   /* if (child.getVisibility() == View.VISIBLE) {
      dependency.setTranslationY(this.calculateTranslation(child));
    } else {
      dependency.setTranslationY(-this.calculateTranslation(child));
    }*/

    return true;
  }

  private void showView(final View view) {
    HeaderBehavior.this.runTranslateAnimation(view, 0, new FastOutSlowInInterpolator(),
        new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationStart(View view) {
            //view.setVisibility(View.VISIBLE);
            //revealFrameLayout.setTranslationY(HeaderBehavior.this.calculateTranslation(view));
          }
        });
  }

  private void hideView(final View view) {
    int height = HeaderBehavior.this.calculateTranslation(view);
    int translateY = -height;
    HeaderBehavior.this.runTranslateAnimation(view, translateY, new FastOutLinearInInterpolator(),
        new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            //view.setVisibility(View.GONE);

            //revealFrameLayout.setTranslationY(-HeaderBehavior.this.calculateTranslation(view));
          }
        });
  }

  private int calculateTranslation(View view) {
    int height = view.getHeight();
    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
    int margins = params.topMargin + params.bottomMargin;
    return height + margins;
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
