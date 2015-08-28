package com.app.designmore.helper;

import android.app.Application;
import android.content.Context;
import com.app.designmore.Constants;
import com.app.designmore.greendao.DaoMaster;
import com.app.designmore.greendao.DaoSession;

/**
 * Created by Joker on 2015/8/24.
 */
public class MyApplication extends Application {

  private static DaoMaster daoMaster;
  private static DaoSession daoSession;

  /**
   * 取得DaoMaster
   */
  private static DaoMaster getDaoMaster(Context context) {
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
  protected static DaoSession getDaoSession(Context context) {
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
