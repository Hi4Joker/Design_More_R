package com.app.designmore.greendao;

import android.database.sqlite.SQLiteDatabase;
import com.app.designmore.greendao.dao.UserInfoDao;
import com.app.designmore.greendao.entity.UserInfoEntity;
import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;
import java.util.Map;

/**
 * @see AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

  private final DaoConfig loginDaoConfig;

  private UserInfoDao loginDao;

  public DaoSession(SQLiteDatabase db, IdentityScopeType type,
      Map<Class<? extends AbstractDao<?, ?>>, DaoConfig> daoConfigMap) {
    super(db);

    loginDaoConfig = daoConfigMap.get(UserInfoDao.class).clone();
    loginDaoConfig.initIdentityScope(type);
    loginDao = new UserInfoDao(loginDaoConfig, DaoSession.this);

    DaoSession.this.registerDao(UserInfoEntity.class, loginDao);
  }

  public UserInfoDao getLoginDao() {
    return loginDao;
  }

  public void clear() {
    loginDaoConfig.getIdentityScope().clear();
  }
}
