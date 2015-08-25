package com.joker.app.manager;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import com.joker.app.R;
import com.joker.app.utils.Utils;
import com.joker.app.utils.DensityUtil;
import com.joker.app.revealLib.animation.SupportAnimator;
import com.joker.app.revealLib.animation.ViewAnimationUtils;
import com.joker.app.revealLib.widget.RevealLinearLayout;

/**
 * Created by Joker on 2015/7/24
 */
public class SheetRevealManager extends RecyclerView.OnScrollListener
    implements View.OnAttachStateChangeListener, View.OnClickListener {

  private static final String TAG = SheetRevealManager.class.getSimpleName();
  private static final int ANIMATION_DURATION_SHORT = 300;
  private static final long ANIMATION_DURATION_LONG = 500;
  private static final int MIN_SCROLL_PIXEL = 10;
  private RevealLinearLayout sheetLayout;
  private boolean isSheetShowing;
  private StateListener stateListener;
  private SupportAnimator sheetRevealAnim;
  private float fabX;
  private float fabY;
  private int accumulatedDy;

  public SheetRevealManager() {
  }

 /* private static class SingleHolder {
    private static SheetRevealManager instance = new SheetRevealManager();
  }

  public static SheetRevealManager getInstance() {
    return SingleHolder.instance;
  }*/

  public void attachSheet2View(FloatingActionButton anchorView, @NonNull StateListener listener) {

    this.stateListener = listener;
    fabX = anchorView.getX() + anchorView.getWidth() / 2;
    fabY = anchorView.getY() + anchorView.getHeight() / 2;

    if (sheetLayout == null) {

      sheetLayout = (RevealLinearLayout) View.inflate(anchorView.getContext(),
          R.layout.detail_sheet_reveal_layout, null);
      sheetLayout.findViewById(R.id.detail_sheet_iv).setOnClickListener(SheetRevealManager.this);
      SheetRevealManager.this.attach2Anchor(anchorView);
    } else if (sheetLayout != null && sheetLayout.getParent() == null) {

      SheetRevealManager.this.attach2Anchor(anchorView);
    }
  }

  public void detachSheet2View(View anchorView) {

    if (sheetLayout != null && !isSheetShowing && sheetLayout.getParent() != null) {

      sheetLayout.removeOnAttachStateChangeListener(SheetRevealManager.this);
      ((ViewGroup) anchorView.getRootView().findViewById(android.R.id.content)).removeView(
          sheetLayout);
    }
  }

  private void attach2Anchor(final FloatingActionButton anchorView) {

    sheetLayout.addOnAttachStateChangeListener(SheetRevealManager.this);
    int width = anchorView.getContext().getResources().getDimensionPixelSize(R.dimen.sheet_width);
    int height = anchorView.getContext().getResources().getDimensionPixelSize(R.dimen.sheet_height);

    CoordinatorLayout.LayoutParams layoutParams = new CoordinatorLayout.LayoutParams(width, height);
    ((ViewGroup) anchorView.getRootView().findViewById(android.R.id.content)).addView(sheetLayout,
        layoutParams);

    sheetLayout.getViewTreeObserver()
        .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
          @Override public boolean onPreDraw() {
            sheetLayout.getViewTreeObserver().removeOnPreDrawListener(this);

            /*初始化sheetLayout位置*/
            SheetRevealManager.this.InitialSheetLayoutPosition(anchorView);
            return false;
          }
        });
  }

  private void InitialSheetLayoutPosition(View anchorView) {

   /* int width = anchorView.getContext().getResources().getDisplayMetrics().widthPixels;
    int height = anchorView.getContext().getResources().getDisplayMetrics().heightPixels;*/

    // 得到相对于整个屏幕的区域坐标（左上角坐标——右下角坐标）
    Rect viewRect = new Rect();
    anchorView.getGlobalVisibleRect(viewRect);

    int margin = DensityUtil.dip2px(16);
    int X = viewRect.centerX() - sheetLayout.getWidth() + margin;
    int Y = viewRect.centerY() - sheetLayout.getHeight() + margin;
   /* int X = (width - sheetLayout.getWidth()) / 2;
    int Y = height / 2 + margin;*/

    /*X轴所在位置*/
    ViewCompat.setTranslationX(sheetLayout, X);
    ViewCompat.setTranslationY(sheetLayout, Y);
  }

  /**
   * @hide
   */
  @Deprecated public void attach2RecyclerView(RecyclerView recyclerView) {
    recyclerView.addOnScrollListener(SheetRevealManager.this);
  }

  /**
   * @hide
   */
  @Deprecated public void detach2RecyclerView(RecyclerView recyclerView) {
    recyclerView.removeOnScrollListener(SheetRevealManager.this);
  }

  @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

    if (dy > 0) {/*Scroll Up*/
      accumulatedDy = accumulatedDy > 0 ? accumulatedDy + dy : dy;
      if (accumulatedDy > MIN_SCROLL_PIXEL && isSheetShowing) {

        this.isSheetShowing = false;
        SheetRevealManager.this.disappearSheetReveal();
      }
    } else if (dy < 0) {/*Scroll Down*/
      accumulatedDy = accumulatedDy < 0 ? accumulatedDy + dy : dy;
      if (accumulatedDy < -MIN_SCROLL_PIXEL && isSheetShowing) {

        this.isSheetShowing = false;
        SheetRevealManager.this.disappearSheetReveal();
      }
    }
  }

  @Override public void onViewAttachedToWindow(View v) {

    Log.e(TAG, "SheetLayout AttachedToWindow");
  }

  @Override public void onViewDetachedFromWindow(View v) {

    Log.e(TAG, "SheetLayout DetachedFromWindow");
  }

  @Override public void onClick(View v) {

    switch (v.getId()) {

      case R.id.detail_sheet_iv:
        SheetRevealManager.this.disappearSheetReveal();
        break;
    }
  }

  public void appearSheetReveal(int width, int height) {

    sheetLayout.setVisibility(View.VISIBLE);
    View child = sheetLayout.findViewById(R.id.detail_sheet_root);

    int cx = child.getWidth() / 2;
    int cy = child.getHeight() / 2;
    /*int cx = child.getWidth();
    int cy = child.getHeight();*/
    float startRadius = Utils.pythagorean(width / 2, height / 2);
    float finalRadius = Utils.pythagorean(child.getWidth() / 2, child.getHeight() / 2);

    sheetRevealAnim =
        ViewAnimationUtils.createCircularReveal(child, cx, cy, startRadius, finalRadius);
    sheetRevealAnim.setDuration(ANIMATION_DURATION_SHORT);
    sheetRevealAnim.setInterpolator(new AccelerateInterpolator());
    sheetRevealAnim.addListener(new SupportAnimator.SimpleAnimatorListener() {
      @Override public void onAnimationEnd() {
        SheetRevealManager.this.isSheetShowing = true;
      }
    });

    sheetRevealAnim.start();
  }

  private void disappearSheetReveal() {

    sheetRevealAnim = sheetRevealAnim.reverse();
    sheetRevealAnim.setDuration(ANIMATION_DURATION_SHORT);
    sheetRevealAnim.setInterpolator(new AccelerateInterpolator());
    sheetRevealAnim.addListener(new SupportAnimator.SimpleAnimatorListener() {
      @Override public void onAnimationEnd() {
        SheetRevealManager.this.isSheetShowing = false;

        sheetLayout.setVisibility(View.INVISIBLE);
        stateListener.onSheetHidden();
      }
    });
    sheetRevealAnim.start();
  }

  public void getSheetRect(Rect rect) {

    sheetLayout.getGlobalVisibleRect(rect);
  }

  public boolean isSheetShowing() {

    return isSheetShowing;
  }

  public interface StateListener {

    void onSheetHidden();
  }
}
