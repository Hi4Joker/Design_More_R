package com.app.designmore.manager;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.view.MaterialRippleLayout;

/**
 * Created by Joker on 2015/9/20.
 *
 * https://github.com/mzgreen/HideOnScrollExample/blob/master/app/src/main/java/pl/michalz/hideonscrollexample/ScrollingFABBehavior.java
 */
public class BottomDependBehavior extends CoordinatorLayout.Behavior<MaterialRippleLayout> {

  private static final String TAG = BottomDependBehavior.class.getCanonicalName();
  private float actionBarSize;

  public BottomDependBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.actionBarSize = DensityUtil.getActionBarSize(context);
  }

  @Override public boolean layoutDependsOn(CoordinatorLayout parent, MaterialRippleLayout child, View dependency) {
    return dependency instanceof AppBarLayout;
  }

  @Override
  public boolean onDependentViewChanged(CoordinatorLayout parent, MaterialRippleLayout child, View dependency) {

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
