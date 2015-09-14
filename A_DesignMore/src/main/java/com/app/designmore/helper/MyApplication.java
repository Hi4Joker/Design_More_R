package com.app.designmore.helper;

import android.app.Application;
import android.content.Context;
import com.app.designmore.Constants;
import com.app.designmore.greendao.DaoMaster;
import com.app.designmore.greendao.DaoSession;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

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
    refWatcher = LeakCanary.install(this);
  }

  /**
   * 取得DaoMaster
   */
  private  DaoMaster getDaoMaster(Context context) {
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
   * 取得DaoSession
   */
  protected  DaoSession getDaoSession(Context context) {
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
