package com.app.designmore.greendao.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import com.app.designmore.greendao.entity.UserInfoEntity;
import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

/**
 * Created by Joker on 2015/8/24.
 */
public class UserInfoDao extends AbstractDao<UserInfoEntity, Long> {

  public static final String TABLENAME = "LOGIN";

  public static class Properties {
    public final static Property Id = new Property(0, Long.class, "id", true, "_id");
    public final static Property userId = new Property(1, String.class, "userId", true, "USER_ID");
    public final static Property userName =
        new Property(2, String.class, "userName", true, "USER_NAME");
    public final static Property phone = new Property(3, String.class, "phone", true, "PHONE");
    public final static Property sex = new Property(4, String.class, "sex", true, "SEX");
    public final static Property email = new Property(5, String.class, "email", true, "EMAIL");
  }

  public UserInfoDao(DaoConfig config) {
    super(config);
  }

  public UserInfoDao(DaoConfig config, AbstractDaoSession daoSession) {
    super(config, daoSession);
  }

  /*******************************************************************************************************/
  /** Creates the underlying database table. */
  public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
    String constraint = ifNotExists ? "IF NOT EXISTS " : "";
    db.execSQL("CREATE TABLE " + constraint + "'LOGIN' (" + //
        "'_id' INTEGER PRIMARY KEY ," + // 0: id
        "'USER_ID' TEXT NOT NULL ," + // 1: userId
        "'USER_NAME' TEXT NOT NULL ," + // 2: userName
        "'PHONE' TEXT NOT NULL ," + // 3: phone
        "'SEX' TEXT NOT NULL ," + // 4: sex
        "'EMAIL' TEXT NOT NULL );"); // 5: email
  }

  /** Drops the underlying database table. */
  public static void dropTable(SQLiteDatabase db, boolean ifExists) {
    String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'LOGIN'";
    db.execSQL(sql);
  }
  //  /**********************************************/

  @Override protected void bindValues(SQLiteStatement stmt, UserInfoEntity entity) {
    stmt.clearBindings();

    Long id = entity.get_id();
    if (id != null) {
      stmt.bindLong(1, id);
    }
    String userId = entity.getUserId();
    if (userId != null) {
      stmt.bindString(2, userId);
    }
    String userName = entity.getUserId();
    if (userId != null) {
      stmt.bindString(3, userName);
    }

    String phone = entity.getPhone();
    if (userId != null) {
      stmt.bindString(4, phone);
    }
    String sex = entity.getPhone();
    if (userId != null) {
      stmt.bindString(5, sex);
    }
    String emali = entity.getPhone();
    if (userId != null) {
      stmt.bindString(6, emali);
    }
  }

  @Override protected UserInfoEntity readEntity(Cursor cursor, int offset) {

    UserInfoEntity entity = new UserInfoEntity(//
        cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0),
        cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1),
        cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2),
        cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3),
        cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4),
        cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
    return entity;
  }

  @Override protected void readEntity(Cursor cursor, UserInfoEntity entity, int offset) {

    entity.set_id(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
    entity.setUserId(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
    entity.setUserName(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
    entity.setPhone(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
    entity.setSex(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
    entity.setEmail(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
  }

  @Override protected Long readKey(Cursor cursor, int offset) {
    return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
  }

  @Override protected Long updateKeyAfterInsert(UserInfoEntity entity, long rowId) {
    entity.set_id(rowId);
    return rowId;
  }

  @Override protected Long getKey(UserInfoEntity entity) {
    if (entity != null) {
      return entity.get_id();
    } else {
      return null;
    }
  }

  @Override protected boolean isEntityUpdateable() {
    return true;
  }
}
