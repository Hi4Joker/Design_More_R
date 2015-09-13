package com.app.designmore.greendao;

import android.database.sqlite.SQLiteDatabase;
import com.app.designmore.greendao.dao.LoginInfoDao;
import com.app.designmore.greendao.entity.Dao_LoginInfo;
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

  /*登陆信息Dao*/
  private LoginInfoDao loginDao;

  public DaoSession(SQLiteDatabase db, IdentityScopeType type,
      Map<Class<? extends AbstractDao<?, ?>>, DaoConfig> daoConfigMap) {
    super(db);

    /*Dao三部曲*/
    loginDaoConfig = daoConfigMap.get(LoginInfoDao.class).clone();
    loginDaoConfig.initIdentityScope(type);
    loginDao = new LoginInfoDao(loginDaoConfig, DaoSession.this);

    DaoSession.this.registerDao(Dao_LoginInfo.class, loginDao);
  }

  public LoginInfoDao getLoginDao() {
    return loginDao;
  }

  public void clear() {
    loginDaoConfig.getIdentityScope().clear();
  }
}
