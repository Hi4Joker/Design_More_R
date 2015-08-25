package com.joker.app.manager;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import java.util.HashMap;

/**
 * Created by Joker on 2015/7/22.
 */
public class Scroller extends RecyclerView.OnScrollListener {

  private static final int MIN_SCROLL_PIXEL = 10;
  private static final String TAG = Scroller.class.getSimpleName();
  private boolean hidden;
  private int totalDy;
  private int accumulatedDy;
  private int thresholdOffset;
  private HashMap<View, Direction> viewsToHide = new HashMap<>();
  private boolean isAddListener;
  private Interpolator showInterpolator;
  private Interpolator hideInterpolator;
  private boolean isLock;
  private boolean isSnacking;

  public void lockAnim(boolean isLock) {
    this.isLock = isLock;
  }

  public void notifySnacking() {
    this.isSnacking = true;
  }

  public enum Direction {UP, DOWN}

  public Scroller(ScrollBuilder scrollBuilder) {

    this.showInterpolator = scrollBuilder.showInterpolator;
    this.hideInterpolator = scrollBuilder.hideInterpolator;
    this.thresholdOffset = scrollBuilder.offset;

    for (View view : scrollBuilder.viewsToHide.keySet()) {
      this.viewsToHide.put(view, scrollBuilder.viewsToHide.get(view));
    }
  }

  public static ScrollBuilder builder() {
    return new ScrollBuilder();
  }

  public void attach(@NonNull RecyclerView recyclerView) {

    if (!isAddListener) {
      this.isAddListener = !isAddListener;
      recyclerView.addOnScrollListener(this);
    }
  }

  public void detach(@NonNull RecyclerView recyclerView) {

    if (isAddListener) {
      this.isAddListener = !isAddListener;
      recyclerView.removeOnScrollListener(this);
    }
  }

  @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

    /**
     * Scroll up +dy
     * Scroll down -dy
     */
    this.totalDy += dy;

    if (totalDy < thresholdOffset) {
      return;
    }

    if (dy > 0) {/*Scroll Up*/
      accumulatedDy = accumulatedDy > 0 ? accumulatedDy + dy : dy;
      if (accumulatedDy > MIN_SCROLL_PIXEL && !isLock) {
        Scroller.this.hideViews();
      }
    } else if (dy < 0) {/*Scroll Down*/
      accumulatedDy = accumulatedDy < 0 ? accumulatedDy + dy : dy;
      if (accumulatedDy < -MIN_SCROLL_PIXEL && !isLock) {
        Scroller.this.showViews();
      }
    }
  }

  public void hideViews() {
    if (!hidden) {
      this.hidden = true;
      for (View view : viewsToHide.keySet()) {
        Scroller.this.hideView(view, viewsToHide.get(view));
      }
    }
  }

  private void hideView(View view, Direction direction) {

    if (view instanceof FloatingActionButton
        && view.getTag() != null
        && view.getTag() instanceof Snackbar) {

      if (((Snackbar) view.getTag()).getView().getParent() != null) {

        this.hidden = false;
        ((Snackbar) view.getTag()).dismiss();
        return;
      }
    }

    if (hidden) {
      int height = Scroller.this.calculateTranslation(view);
      int translateY = direction == Direction.UP ? -height : height;
      if (hideInterpolator == null) hideInterpolator = new LinearInterpolator();
      Scroller.this.runTranslateAnimation(view, translateY, hideInterpolator);
    }
  }

  private int calculateTranslation(View view) {
    int height = view.getHeight();

    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
    int margins = params.topMargin + params.bottomMargin;

    return height + margins;
  }

  private void showViews() {
    if (hidden || isSnacking) {
      this.hidden = false;
      this.isSnacking = false;
      for (View view : viewsToHide.keySet()) {
        Scroller.this.showView(view);
      }
    }
  }

  private void showView(View view) {

    if (view instanceof FloatingActionButton
        && view.getTag() != null
        && view.getTag() instanceof Snackbar) {

      if (((Snackbar) view.getTag()).getView().getParent() != null) {

        this.hidden = true;
        ((Snackbar) view.getTag()).dismiss();
        return;
      }
    }

    if (!hidden) {
      if (showInterpolator == null) showInterpolator = new OvershootInterpolator(1.6f);
      Scroller.this.runTranslateAnimation(view, 0, showInterpolator);
    }
  }

  private void runTranslateAnimation(View target, int translateY, Interpolator interpolator) {

    Animator slideInAnimation = ObjectAnimator.ofFloat(target, "translationY", translateY);
    slideInAnimation.setDuration(
        target.getContext().getResources().getInteger(android.R.integer.config_longAnimTime));
    slideInAnimation.setInterpolator(interpolator);
    slideInAnimation.start();
  }

  public void reset() {

    if (viewsToHide != null) viewsToHide.clear();
  }
}
