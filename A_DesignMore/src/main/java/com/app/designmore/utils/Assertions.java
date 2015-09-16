package com.app.designmore.utils;

import android.os.Looper;

/**
 * Created by Joker on 2015/9/16.
 */
public final class Assertions {
  private Assertions() {
    throw new AssertionError("No instances");
  }

  public static void assertUiThread() {
    if (Looper.getMainLooper() != Looper.myLooper()) {
      throw new IllegalStateException(
          "Observers must subscribe from the main UI thread, but was " + Thread.currentThread());
    }
  }
}