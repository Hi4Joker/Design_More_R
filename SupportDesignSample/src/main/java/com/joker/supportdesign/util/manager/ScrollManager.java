/*
 * Copyright (C) 2015 Antonio Leiva
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.joker.supportdesign.util.manager;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import java.util.HashMap;

public class ScrollManager extends RecyclerView.OnScrollListener {

  private static final int MIN_SCROLL_TO_HIDE = 10;
  private static final int ANIMATION_DURATION = 700;
  private boolean hidden;
  private int accummulatedDy;
  private int totalDy;
  private int initialOffset;
  private HashMap<View, Direction> viewsHold = new HashMap<>();

  public enum Direction {UP, DOWN}

  public ScrollManager() {
  }

  public void attach(RecyclerView recyclerView) {
    recyclerView.addOnScrollListener(this);
  }

  public void detach(RecyclerView recyclerView) {
    recyclerView.removeOnScrollListener(this);
  }

  public void addView(View view, Direction direction) {
    if (!viewsHold.containsKey(view)) {
      viewsHold.put(view, direction);
    }
  }

  public void setInitialOffset(int initialOffset) {
    this.initialOffset = initialOffset - 10;
  }

  @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
    totalDy += dy;

    if (totalDy < initialOffset) {
      return;
    }

    if (dy > 0) {
      accummulatedDy = accummulatedDy > 0 ? accummulatedDy + dy : dy;
      if (accummulatedDy > MIN_SCROLL_TO_HIDE) {
        ScrollManager.this.hideView();
      }
    } else if (dy < 0) {
      accummulatedDy = accummulatedDy < 0 ? accummulatedDy + dy : dy;
      if (accummulatedDy < -MIN_SCROLL_TO_HIDE) {
        ScrollManager.this.showView();
      }
    }
  }

  public void hideView() {
    if (!hidden) {
      hidden = true;
      for (View view : viewsHold.keySet()) {
        ScrollManager.this.hideAnimation(view, viewsHold.get(view));
      }
    }
  }

  private void showView() {
    if (hidden) {
      hidden = false;
      for (View view : viewsHold.keySet()) {
        ScrollManager.this.showAnimation(view);
      }
    }
  }

  private int calculateTranslation(View view) {
    int height = view.getHeight();
    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
    int margins = params.topMargin + params.bottomMargin;
    return height + margins;
  }

  private void showAnimation(View view) {
    runTranslateAnimation(view, 0, new OvershootInterpolator(1.6f));
  }

  private void hideAnimation(View view, Direction direction) {
    int height = calculateTranslation(view);
    int translateY = direction == Direction.UP ? -height : height;
    runTranslateAnimation(view, translateY, new AccelerateInterpolator(2.0f));
  }

  private void runTranslateAnimation(View view, int translateY, Interpolator interpolator) {

    view.animate()
        .translationY(translateY)
        .setInterpolator(interpolator)
        .setDuration(
            view.getContext().getResources().getInteger(android.R.integer.config_longAnimTime));

    /*Animator slideInAnimation = ObjectAnimator.ofFloat(view, "translationY", translateY);
    slideInAnimation.setInterpolator(interpolator);
    //slideInAnimation.setDuration(ANIMATION_DURATION);
    slideInAnimation.setDuration(
        view.getContext().getResources().getInteger(android.R.integer.config_longAnimTime));
    slideInAnimation.start();*/
  }
}
