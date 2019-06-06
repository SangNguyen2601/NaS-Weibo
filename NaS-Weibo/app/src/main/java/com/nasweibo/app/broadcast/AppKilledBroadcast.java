package com.nasweibo.app.broadcast;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.nasweibo.app.services.NewMessageService;


public class AppKilledBroadcast extends BroadcastReceiver {
    private NewMessageService newMessageService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NewMessageService.LocalBinder binder = (NewMessageService.LocalBinder) service;
            newMessageService = binder.getService();
            newMessageService.setShowNoti(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
//        Intent intent2 = new Intent(context, NewMessageService.class);
//        context.startService(intent2);
//        context.bindService(intent2, serviceConnection, Context.BIND_AUTO_CREATE);
    }
}
