package com.app.designmore.helper;

import android.content.Context;
import com.app.designmore.Constants;
import com.app.designmore.greendao.DaoSession;
import com.app.designmore.greendao.dao.UserInfoDao;
import com.app.designmore.greendao.entity.UserInfoEntity;
import com.app.designmore.utils.PreferencesUtils;
import de.greenrobot.dao.query.QueryBuilder;

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

  public long saveLoginInfo(UserInfoEntity loginInfo) {
    return getLoginDao().insertOrReplace(loginInfo);
  }

  public void deleteLoginInfo(UserInfoEntity loginInfo) {
    getLoginDao().delete(loginInfo);
  }

  public UserInfoEntity getCurrentUser(Context context) {

    return DBHelper.this.getUser(PreferencesUtils.getString(context, Constants.CURRENT_USER, "-1"));
  }

  private UserInfoEntity getUser(String phone) {
    QueryBuilder<UserInfoEntity> queryBuilder = getLoginDao().queryBuilder();
    queryBuilder.where(UserInfoDao.Properties.phone.eq(phone));
    if (queryBuilder.list().size() > 0) {
      return queryBuilder.list().get(0);
    } else {
      return null;
    }
  }

  private UserInfoDao getLoginDao() {

    return daoSession.getLoginDao();
  }
}
