package com.app.designmore.greendao.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import com.app.designmore.greendao.entity.Dao_LoginInfo;
import com.app.designmore.retrofit.entity.LoginEntity;
import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

/**
 * Created by Joker on 2015/8/24.
 */
public class LoginInfoDao extends AbstractDao<Dao_LoginInfo, Long> {

  public static final String TABLENAME = "LOGIN";

  public static class Properties {
    public final static Property Id = new Property(0, Long.class, "id", true, "_id");
    public final static Property userId = new Property(1, String.class, "userId", true, "USER_ID");
    public final static Property addressId =
        new Property(2, String.class, "addressId", true, "ADDRESS_ID");
  }

  public LoginInfoDao(DaoConfig config) {
    super(config);
  }

  public LoginInfoDao(DaoConfig config, AbstractDaoSession daoSession) {
    super(config, daoSession);
  }

  /*******************************************************************************************************/
  /** Creates the underlying database table. */
  public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
    String constraint = ifNotExists ? "IF NOT EXISTS " : "";
    db.execSQL("CREATE TABLE " + constraint + "'LOGIN' (" + //
        "'_id' INTEGER PRIMARY KEY ," + // 0: id
        "'USER_ID' TEXT NOT NULL ," + // 1: userId
        "'ADDRESS_ID' TEXT NOT NULL );"); // 2: addressId
  }

  /** Drops the underlying database table. */
  public static void dropTable(SQLiteDatabase db, boolean ifExists) {
    String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'LOGIN'";
    db.execSQL(sql);
  }
  //  /**********************************************/

  @Override protected void bindValues(SQLiteStatement stmt, Dao_LoginInfo entity) {
    stmt.clearBindings();

    Long id = entity.get_id();
    if (id != null) {
      stmt.bindLong(1, id);
    }
    String userId = entity.getUserId();
    if (userId != null) {
      stmt.bindString(2, userId);
    }
    String addressId = entity.getAddressId();
    if (addressId != null) {
      stmt.bindString(3, addressId);
    }
  }

  @Override protected Dao_LoginInfo readEntity(Cursor cursor, int offset) {

    Dao_LoginInfo entity = new Dao_LoginInfo(//
        cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0),
        cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1),
        cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));

    return entity;
  }

  @Override protected void readEntity(Cursor cursor, Dao_LoginInfo entity, int offset) {

    entity.set_id(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
    entity.setUserId(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
    entity.setAddressId(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
  }

  @Override protected Long readKey(Cursor cursor, int offset) {
    return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
  }

  @Override protected Long updateKeyAfterInsert(Dao_LoginInfo entity, long rowId) {
    entity.set_id(rowId);
    return rowId;
  }

  @Override protected Long getKey(Dao_LoginInfo entity) {
    if (entity != null) {
      return entity.get_id();
    } else {
      return null;
    }
  }

  @Override public void delete(Dao_LoginInfo entity) {
    super.delete(entity);
  }

  @Override protected boolean isEntityUpdateable() {
    return true;
  }
}
