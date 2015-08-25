package com.joker.app.view;

import android.content.res.Resources;

/**
 * dp是一个与屏幕密度有关系的单位，dp与像素的换算关系为 px = dp * (dpi / 160)
 * 例如在240密度（dpi）的屏幕上一个dp等于1.5个像素。
 */

public class DensityUtil {

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
}