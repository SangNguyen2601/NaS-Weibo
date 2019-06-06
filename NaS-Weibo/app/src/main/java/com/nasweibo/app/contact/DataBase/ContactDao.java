package com.nasweibo.app.contact.DataBase;

import android.content.Context;

import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.assit.QueryBuilder;

import java.util.List;


public class ContactDao {
  private static final String TAG = "StationDao";

  private Context mContext;
  private LiteOrm mLiteOrm;
  private static ContactDao contactDao;

  public static synchronized ContactDao getInstance(Context context) {
    if (contactDao == null)
      contactDao = new ContactDao(context, LiteOrmHelper.getInstance(context));
    return contactDao;
  }

  public ContactDao(Context context, LiteOrm orm) {
    mContext = context;
    mLiteOrm = orm;
  }

  public long insert(List<PeopleEntity> contactEntities) {
    if (null == contactEntities) return -1;

    deleteAll();
    long result = mLiteOrm.save(contactEntities);
    return result;
  }

  public long insert(PeopleEntity contactEntity) {
    long result = mLiteOrm.insert(contactEntity);
    return result;
  }

  public int update(PeopleEntity contactEntity) {
    int result = mLiteOrm.update(contactEntity);
    return result;
  }

  public int delete(PeopleEntity contactEntity) {
    int result = mLiteOrm.delete(contactEntity);
    return result;
  }

  public PeopleEntity findById(String id) {

    try {
      QueryBuilder queryBuilder = new QueryBuilder<>(PeopleEntity.class)
          .whereEquals(PeopleEntity.ID_FRIEND, id);

      return (PeopleEntity) mLiteOrm.query(
          queryBuilder
      ).get(0);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public List<PeopleEntity> getAllContact() {
    List<PeopleEntity> contactEntities = mLiteOrm.query(
        new QueryBuilder<>(PeopleEntity.class)
    );
    return contactEntities;
  }

  public void deleteAll() {
    mLiteOrm.deleteAll(PeopleEntity.class);
  }

}
