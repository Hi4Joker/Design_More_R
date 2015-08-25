package com.joker.app;

import android.app.Application;

/**
 * Created by Administrator on 2015/7/27.
 */
public class MyApp extends Application {

  //private RefWatcher refWatcher;

  @Override public void onCreate() {
    super.onCreate();

    //refWatcher = LeakCanary.install(MyApp.this);
  }
}
