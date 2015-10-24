package com.app.designmore.utils;

import android.os.Looper;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created by Joker on 2015/7/25.
 */
public class Utils {

  /*正则表达式：验证手机号*/
  public static final String REGEX_MOBILE = "^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
  /*正则表达式：验证邮编*/
  public static final String REGEX_ZIPCODE = "^[0-9]\\d{5}$";
  /*正则表达式：验证用户名*/
  //public static final String REGEX_USERNAME = "^[a-zA-Z]\\w{5,17}$";
  //public static final String REGEX_USERNAME = "^[a-zA-Z0-9_]{3,16}$";

  /*勾股定理*/
  public static float pythagorean(int width, int height) {
    return (float) Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2));
  }

  /*计算文件大小*/
  public static String FormetFileSize(long fileS) {
    DecimalFormat df = new DecimalFormat("#.00");
    String fileSizeString = "";
    if (fileS < 1024) {
      fileSizeString = df.format((double) fileS) + "B";
    } else if (fileS < 1048576) {
      fileSizeString = df.format((double) fileS / 1024) + "K";
    } else if (fileS < 1073741824) {
      fileSizeString = df.format((double) fileS / 1048576) + "M";
    } else {
      fileSizeString = df.format((double) fileS / 1073741824) + "G";
    }
    return fileSizeString;
  }

  /*校验手机号*/
  public static boolean isMobile(String mobile) {
    return Pattern.matches(REGEX_MOBILE, mobile);
  }

  /*校验手机号*/
  public static boolean isZipCode(String zipcode) {
    return Pattern.matches(REGEX_ZIPCODE, zipcode);
  }

  public static String dateFormat(long time) {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("", Locale.SIMPLIFIED_CHINESE);
    simpleDateFormat.applyPattern("MMddHHmmss");
    return simpleDateFormat.format(time);
  }

  public static boolean isUiThread() {
    return Looper.getMainLooper() != Looper.myLooper();
  }

  public static void checkUiThread() {
    if (Looper.getMainLooper() != Looper.myLooper()) {
      throw new IllegalStateException(
          "Must be called from the main thread. Was: " + Thread.currentThread());
    }
  }
}
