package com.app.designmore.helper;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import com.app.designmore.Constants;
import com.app.designmore.greendao.DaoMaster;
import com.app.designmore.greendao.DaoSession;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.GINGERBREAD;

/**
 * Created by Joker on 2015/8/24.
 */
public class MyApplication extends Application {

  private static DaoMaster daoMaster;
  private static DaoSession daoSession;

  private static MyApplication instance;
  private RefWatcher refWatcher;

  public static MyApplication get() {
    return instance;
  }

  public static RefWatcher getRefWatcher() {
    return MyApplication.get().refWatcher;
  }

  @Override public void onCreate() {
    super.onCreate();

    instance = (MyApplication) getApplicationContext();

    MyApplication.this.enabledStrictMode();
    refWatcher = LeakCanary.install(this);
  }

  private void enabledStrictMode() {
    if (SDK_INT >= GINGERBREAD) {
      StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder() //
          .detectAll()  //
          .penaltyLog() //
          .penaltyDeath() //
          .build());
    }
  }

  /**
   * 获取DaoMaster
   */
  private DaoMaster getDaoMaster(Context context) {
    if (daoMaster == null) {
      synchronized (MyApplication.class) {
        if (daoMaster == null) {
          DaoMaster.OpenHelper helper =
              new DaoMaster.DevOpenHelper(context, Constants.DB_NAME, null);
          daoMaster = new DaoMaster(helper.getWritableDatabase());
        }
      }
    }
    return daoMaster;
  }

  /**
   * 获取DaoSession
   */
  protected DaoSession getDaoSession(Context context) {
    if (daoSession == null) {

      synchronized (MyApplication.class) {
        if (daoSession == null) {
          if (daoMaster == null) {
            daoMaster = getDaoMaster(context);
          }
          daoSession = daoMaster.newSession();
        }
      }
    }
    return daoSession;
  }
}
