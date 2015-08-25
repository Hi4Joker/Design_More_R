package com.joker.supportdesign.util.manager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import com.joker.supportdesign.mvp.domain.ForecastEntity;
import com.joker.supportdesign.ui.WeatherLayout;
import com.joker.supportdesign.util.DensityUtil;
import java.util.HashMap;

/**
 * Created by froger_mcs on 16.12.14.
 */
public class WeatherLayoutManager extends RecyclerView.OnScrollListener
    implements View.OnAttachStateChangeListener {

  private static final String TAG = WeatherLayoutManager.class.getSimpleName();

  private WeatherLayout weatherLayout;

  private boolean isWeatherShowing = false;

  private WeatherLayoutManager() {
  }

  public static WeatherLayoutManager getInstance() {
    return SingleHolder.instance;
  }

  private static class SingleHolder {
    private static WeatherLayoutManager instance = new WeatherLayoutManager();
  }

  public void toggleWeatherLayout(View actionView, WeatherLayout.OnItemClickListener listener) {
    if (weatherLayout == null && !isWeatherShowing) {
      WeatherLayoutManager.this.showWeatherByView(actionView, listener);
    } else if (weatherLayout != null && isWeatherShowing) {
      WeatherLayoutManager.this.hideWeatherLayout();
    }
  }

  private void showWeatherByView(final View actionView,
      WeatherLayout.OnItemClickListener listener) {

    isWeatherShowing = true;

    weatherLayout = new WeatherLayout(actionView.getContext());
    weatherLayout.addOnAttachStateChangeListener(this);
    weatherLayout.addOnItemClickListener(listener);

   /* ViewGroup viewGroup = (ViewGroup) actionView.getRootView();
    ViewGroup viewGroup1 = (ViewGroup) actionView.getRootView().findViewById(android.R.id.content);

      *//*com.android.internal.policy.impl.PhoneWindow$DecorView*//*
    Log.e("FeedContextMenuManager", "RootView::::" + viewGroup.toString());
      *//*android.support.v7.internal.widget.NativeActionModeAwareLayout*//*
    Log.e("FeedContextMenuManager", "content::::" + viewGroup1.toString());*/

    ((ViewGroup) actionView.getRootView().findViewById(android.R.id.content)).addView(
        weatherLayout);

     /* ((ViewGroup) ((ViewGroup) actionView.getRootView()
          .findViewById(android.R.id.content)).getChildAt(0)).addView(weatherLayout);*/

    weatherLayout.getViewTreeObserver()
        .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
          @Override public boolean onPreDraw() {
            weatherLayout.getViewTreeObserver().removeOnPreDrawListener(this);

            /*初始化WeatherLayout位置*/
            WeatherLayoutManager.this.setupContextMenuInitialPosition(actionView);

            /*WeatherLayout显示动画*/
            WeatherLayoutManager.this.performShowAnimation();
            return false;
          }
        });
  }

  private void setupContextMenuInitialPosition(View actionView) {

    //int statusBarHeight = 0;
    //int resourceId = actionView.getContext()
    //    .getResources()
    //    .getIdentifier("status_bar_height", "dimen", "android");
    //if (resourceId > 0) {
    //  statusBarHeight = actionView.getResources().getDimensionPixelSize(resourceId);
    //}
    ///*statusBarHeight:60*/
    //Log.e(TAG, "statusBarHeight:" + statusBarHeight);

    //int width = actionView.getContext().getResources().getDisplayMetrics().widthPixels; // 屏幕宽度（像素）
    //int height =
    //    actionView.getContext().getResources().getDisplayMetrics().heightPixels; // 屏幕高度（像素）
    //Log.e(TAG, "width:" + width + ",height:" + height);

    ////得到view的左上角坐标（相对于整个屏幕）
    //final int[] actionViewLocation = new int[2];
    //actionView.getLocationOnScreen(actionViewLocation);
    ///*getLocationOnScreen:912,60*/
    //Log.e(TAG, "getLocationOnScreen:" + actionViewLocation[0] + "," + actionViewLocation[1]);

    ////得到这个view左上角的坐标（相对于当前Activity）
    //int[] position = new int[2];
    //actionView.getLocationInWindow(position);
    ///*getLocationInWindow:912,60*/
    //Log.e(TAG, "getLocationInWindow:" + position[0] + "," + position[1]);


    /*获取屏幕宽度*/
    int width = actionView.getContext().getResources().getDisplayMetrics().widthPixels;

    // 得到相对于整个屏幕的区域坐标（左上角坐标——右下角坐标）
    Rect viewRect = new Rect();
    actionView.getGlobalVisibleRect(viewRect);
    /*Rect(912, 60 - 1080, 228)*/
    Log.e(TAG, viewRect.toString());

    int initialMargin = width - DensityUtil.dip2px(10);

    int X = width - initialMargin;
    int Y = (viewRect.bottom - viewRect.top) / 2;

    /*X轴所在位置*/
    weatherLayout.setTranslationX(X);
    /*Y轴所在位置*/
    weatherLayout.setTranslationY(Y);
  }

  private void performShowAnimation() {

    weatherLayout.setPivotX(weatherLayout.getWidth());
    weatherLayout.setPivotY(0.0f);
    weatherLayout.setScaleX(0.1f);
    weatherLayout.setScaleY(0.1f);
    weatherLayout.animate()
        .scaleX(1.0f)
        .scaleY(1.0f)
        .setDuration(600)
        .setInterpolator(new OvershootInterpolator(1.4f))
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationEnd(Animator animation) {

            if (weatherWindowListener != null) {
              weatherWindowListener.onWeatherShow();
            }
          }
        });
  }

  private void performDismissAnimation() {
    weatherLayout.setPivotX(weatherLayout.getWidth());
    weatherLayout.setPivotY(0.0f);
    weatherLayout.setAlpha(1.0f);
    weatherLayout.animate()
        .scaleX(0.1f)
        .scaleY(0.1f)
        .alpha(0.1f)
        .setDuration(400)
        .setInterpolator(new AccelerateInterpolator())
        .setStartDelay(100)
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationEnd(Animator animation) {

            if (weatherLayout != null) {
              weatherLayout.dismiss();
            }

            if (weatherWindowListener != null) {
              weatherWindowListener.onWeatherHide();
              weatherWindowListener = null;
            }
          }
        });
  }

  public void onScrolled(int dy) {
    if (weatherLayout != null) {
      weatherLayout.setTranslationY(weatherLayout.getTranslationY() - dy);
      if (isWeatherShowing) {
        hideWeatherLayout();
      }
    }
  }

  public void hideWeatherLayout() {
    isWeatherShowing = false;
    /*取消之前的显示动画，如果有的话*/
    weatherLayout.animate().cancel();
    WeatherLayoutManager.this.performDismissAnimation();
  }

  public void setWeather(HashMap<String, ForecastEntity> weatherData) {
    weatherLayout.fillData(weatherData);
  }

  public void setWeatherState(@WeatherLayout.Status int state) {
    weatherLayout.setCurrentState(state);
  }

  public boolean isShowing() {
    return weatherLayout != null && isWeatherShowing;
  }

  @Override public void onViewAttachedToWindow(View v) {
  }

  @Override public void onViewDetachedFromWindow(View v) {
    weatherLayout = null;
  }

  private WeatherWindowListener weatherWindowListener;

  public void setWindowChangeListener(WeatherWindowListener weatherWindowListener) {
    WeatherLayoutManager.this.weatherWindowListener = weatherWindowListener;
  }

  public interface WeatherWindowListener {

    void onWeatherShow();

    void onWeatherHide();
  }
}
