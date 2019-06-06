package com.nasweibo.app.services;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.nasweibo.app.data.ContactGroup;
import com.nasweibo.app.data.source.DataSource;
import com.nasweibo.app.data.source.remote.RemoteData;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class DetectOnlineStateService extends Service {

  public static String TAG = "DetectOnlineStateService";
  private final IBinder mBinder = new LocalBinder();
  UpdateState updateState;
  Timer timer;
  RemoteData remoteData;

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
    remoteData = new RemoteData(DetectOnlineStateService.this);

    timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        loadStateOnline();
      }
    }, 10, 30 * 1000);
  }

  public void loadStateOnline(){
    remoteData.getContactList(new DataSource.LoadDataListener() {
      @Override
      public void onDataLoaded(Object data) {
        List<ContactGroup> contactGroupList = (List<ContactGroup>) data;
        if (updateState != null) updateState.onUpdateState(contactGroupList);
        Log.d(TAG, "get contact state");
      }

      @Override
      public void onDataNotAvailable() {
      }

      @Override
      public void onError(String cause) {
      }
    });
  }

  public void setUpdateState(UpdateState updateState) {
    this.updateState = updateState;
  }

  /**
   * Class used for the client Binder.  Because we know this service always
   * runs in the same process as its clients, we don't need to deal with IPC.
   */
  public class LocalBinder extends Binder {
    public DetectOnlineStateService getService() {
      // Return this instance of LocalService so clients can call public methods
      return DetectOnlineStateService.this;
    }
  }

  public interface UpdateState {
    void onUpdateState(List<ContactGroup> peopleList);
  }

  @Override
  public void onDestroy() {
    if (timer != null)
      timer.cancel();
    super.onDestroy();
  }
}
