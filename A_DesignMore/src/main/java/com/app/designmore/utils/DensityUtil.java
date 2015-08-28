package com.app.designmore.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

/**
 * dp是一个与屏幕密度有关系的单位，dp与像素的换算关系为 px = dp * (dpi / 160)
 * 例如在240密度（dpi）的屏幕上一个dp等于1.5个像素。
 */

public class DensityUtil {

  private static int screenHeight;
  private static int screenWidth;
  private static int statusBarHeight;

  /**
   * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
   */
  public static int dip2px(float dpValue) {
    final float scale = Resources.getSystem().getDisplayMetrics().density;
    return (int) (dpValue * scale + 0.5f);
  }

  /**
   * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
   */
  public static int px2dip(float pxValue) {
    final float scale = Resources.getSystem().getDisplayMetrics().density;
    return (int) (pxValue / scale + 0.5f);
  }

  /**
   * 将px值转换为sp值
   */
  public static int px2sp(Context context, float pxValue) {
    final float fontScale = Resources.getSystem().getDisplayMetrics().scaledDensity;
    return (int) (pxValue / fontScale + 0.5f);
  }

  /**
   * 将sp值转换为px值
   */
  public static int sp2px(Context context, float spValue) {
    final float fontScale = Resources.getSystem().getDisplayMetrics().scaledDensity;
    return (int) (spValue * fontScale + 0.5f);
  }

  public static int getScreenHeight(Context context) {
    if (screenHeight == 0) {
      WindowManager wm =
          (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
      Display display = wm.getDefaultDisplay();
      Point size = new Point();
      display.getSize(size);
      screenHeight = size.y;
    }

    return screenHeight;
  }

  public static int getScreenWidth(Context context) {
    if (screenWidth == 0) {
      WindowManager wm =
          (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
      Display display = wm.getDefaultDisplay();
      Point size = new Point();
      display.getSize(size);
      screenWidth = size.x;
    }

    return screenWidth;
  }

  public static int getStatusBarHeight(Context context) {

    if (statusBarHeight == 0) {
      int resourceId = context.getApplicationContext()
          .getResources()
          .getIdentifier("status_bar_height", "dimen", "android");
      if (resourceId > 0) {
        statusBarHeight =
            context.getApplicationContext().getResources().getDimensionPixelSize(resourceId);
      }
    }
    return statusBarHeight;
  }

  public static int getLocationY(View item) {

    int[] startingLocation = new int[1];
    // 得到相对于整个屏幕的区域坐标（左上角坐标——右下角坐标）
    Rect viewRect = new Rect();
    item.getGlobalVisibleRect(viewRect);

    startingLocation[0] = (viewRect.top - DensityUtil.getStatusBarHeight(item.getContext())) + (
        viewRect.bottom
            - DensityUtil.getStatusBarHeight(item.getContext()));

    return startingLocation[0] / 2;
  }
}