package com.app.designmore.utils;

import java.text.DecimalFormat;

/**
 * Created by Joker on 2015/7/25.
 */
public class Utils {

  /*勾股定理*/
  public static float pythagorean(int width, int height) {
    return (float) Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2));
  }

  public static String FormetFileSize(long fileS) {// 转换文件大小
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
}
