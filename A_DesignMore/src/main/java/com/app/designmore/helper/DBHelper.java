package com.app.designmore.helper;

import android.content.Context;
import com.app.designmore.Constants;
import com.app.designmore.greendao.DaoSession;
import com.app.designmore.greendao.dao.LoginInfoDao;
import com.app.designmore.greendao.entity.Dao_LoginInfo;
import com.app.designmore.utils.PreferencesUtils;
import de.greenrobot.dao.query.DeleteQuery;
import de.greenrobot.dao.query.Query;

/**
 * Created by Joker on 2015/8/24.
 */
public class DBHelper {

  private static final String TAG = DBHelper.class.getSimpleName();
  private static DBHelper instance;
  private DaoSession daoSession;

  public static DBHelper getInstance(Context context) {
    if (instance == null) {
      synchronized (DBHelper.class) {
        if (instance == null) {
          instance = new DBHelper();
        }
      }
      instance.daoSession = MyApplication.getDaoSession(context);
    }
    return instance;
  }

  public long saveLoginInfo(Context context, Dao_LoginInfo loginInfo) {

    PreferencesUtils.putString(context, Constants.CURRENT_USER_ID, loginInfo.getUserId());
    return getLoginDao().insertOrReplace(loginInfo);
  }

  public void deleteLoginInfo(Context context) {

    DeleteQuery<Dao_LoginInfo> deleteQuery = getLoginDao().queryBuilder()
        .where(LoginInfoDao.Properties.userId.eq(this.getUserID(context)))
        .buildDelete();
    deleteQuery.executeDeleteWithoutDetachingEntities();

    PreferencesUtils.putString(context, Constants.CURRENT_USER_ID, "-1");
  }

  public Dao_LoginInfo getCurrentUser(Context context) {
    Dao_LoginInfo user =
        DBHelper.this.getUser(PreferencesUtils.getString(context, Constants.CURRENT_USER_ID, "-1"));
    return user;
  }

  public String getUserID(Context context) {
    return DBHelper.this.getUser(
        PreferencesUtils.getString(context, Constants.CURRENT_USER_ID, "-1")).getUserId();
  }

  private Dao_LoginInfo getUser(String userId) {

    Query<Dao_LoginInfo> query =
        getLoginDao().queryBuilder().where(LoginInfoDao.Properties.userId.eq(userId)).build();
    if (query.list().size() > 0) {
      return query.list().get(0);
    } else {
      return null;
    }
  }

  private LoginInfoDao getLoginDao() {
    return daoSession.getLoginDao();
  }
}
