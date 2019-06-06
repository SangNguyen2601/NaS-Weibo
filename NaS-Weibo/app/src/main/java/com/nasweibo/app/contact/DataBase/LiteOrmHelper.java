package com.nasweibo.app.contact.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.DataBaseConfig;
import com.litesuits.orm.db.assit.SQLiteHelper;


public class LiteOrmHelper {

  private static final String DB_NAME = "haloapp.db";
  private static final int DB_VERSION = 1;

  private static volatile LiteOrm sInstance;

  private LiteOrmHelper() {
    // Avoid direct instantiate
  }

  public static LiteOrm getInstance(Context context) {
    if (sInstance == null) {
      synchronized (LiteOrmHelper.class) {
        if (sInstance == null) {
//        sInstance = LiteOrm.newCascadeInstance(App.getInstance(), DB_NAME);
          sInstance = LiteOrm.newCascadeInstance
              (new DataBaseConfig(context, DB_NAME, false, DB_VERSION, new SQLiteHelper.OnUpdateListener() {
                @Override
                public void onUpdate(SQLiteDatabase sqLiteDatabase, int i, int i1) {
                  Log.e("LiteOrmHelper", "onUpdate :: version ==>" +
                      sqLiteDatabase.getVersion() + " | i == " + i + " | i1 == " + i1);
                }
              }));
//          sInstance.setDebugged(Configs.LOG_DB);
        }
      }
    }
    return sInstance;
  }
}
