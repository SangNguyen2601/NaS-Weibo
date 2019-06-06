package com.nasweibo.app.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nasweibo.app.util.AccountUtils;
import com.nasweibo.app.util.Constant;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;


public class PushStateOnlineService extends Service {
  public static String TAG = "PushStateOnlineService";
  private final IBinder mBinder = new LocalBinder();
  Timer timer;

  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_NOT_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return mBinder;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    timer = new Timer();
    final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        mDatabase.child(Constant.USER_PREF)
            .child(AccountUtils.getUID(getApplicationContext()))
            .child("status")
            .child("onlinestamp").setValue(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis());
        Log.d(TAG, "Push state online");
      }
    }, 10, 30 * 1000);
  }

  /**
   * Class used for the client Binder.  Because we know this service always
   * runs in the same process as its clients, we don't need to deal with IPC.
   */
  public class LocalBinder extends Binder {
    public PushStateOnlineService getService() {
      // Return this instance of LocalService so clients can call public methods
      return PushStateOnlineService.this;
    }
  }

  @Override
  public void onDestroy() {
    if (timer != null)
      timer.cancel();
    super.onDestroy();
  }
}
