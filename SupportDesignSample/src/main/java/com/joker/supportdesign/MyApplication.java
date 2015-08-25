package com.joker.supportdesign;

import android.app.Application;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * Created by Joker on 2015/6/26.
 */
public class MyApplication extends Application {

  private RefWatcher refWatcher;
  @Override public void onCreate() {
    super.onCreate();

    refWatcher = LeakCanary.install(MyApplication.this);
  }
}
