package com.joker.app.manager;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.joker.app.utils.DensityUtil;

/**
 * Created by Joker on 2015/9/20.
 *
 * https://github.com/mzgreen/HideOnScrollExample/blob/master/app/src/main/java/pl/michalz/hideonscrollexample/ScrollingFABBehavior.java
 */
public class BottomBehavior extends CoordinatorLayout.Behavior<View> {

  private static final String TAG = BottomBehavior.class.getCanonicalName();
  private float actionBarSize;

  public BottomBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.actionBarSize = DensityUtil.getActionBarSize(context);
    Log.e(TAG, "actionBarSize: " + actionBarSize);
  }

  @Override public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
    return dependency instanceof AppBarLayout;
  }

  @Override
  public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {

    boolean returnValue = super.onDependentViewChanged(parent, child, dependency);
    if (dependency instanceof AppBarLayout) {
      CoordinatorLayout.LayoutParams layoutParams =
          (CoordinatorLayout.LayoutParams) child.getLayoutParams();
      int distanceToScroll = child.getHeight() + layoutParams.bottomMargin;
      float ratio = dependency.getY() / actionBarSize;
      child.setTranslationY(-distanceToScroll * ratio);
    }
    return returnValue;
  }
}
