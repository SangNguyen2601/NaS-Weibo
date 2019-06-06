package com.nasweibo.app.contact;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;

import com.nasweibo.app.contact.DataBase.ContactDao;
import com.nasweibo.app.contact.DataBase.MapUtils;
import com.nasweibo.app.data.ContactGroup;
import com.nasweibo.app.data.People;
import com.nasweibo.app.data.source.DataManagement;
import com.nasweibo.app.data.source.DataSource;
import com.nasweibo.app.services.DetectOnlineStateService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;


public class ContactFragPresenter implements ContactFragContract.Presenter {
    private ContactFragContract.View contactFragment;
    private DataManagement dataManagement;
    SharedPreferences sharedpreferences;

    DetectOnlineStateService detectOnlineStateService;
    public static final String KEY_SAVE_LOCAL = "SAVE_LOCAL";
    public static final String KEY_SHARE_PREFERENCE_CONTACT = "CONTACT";

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DetectOnlineStateService.LocalBinder binder = (DetectOnlineStateService.LocalBinder) service;
            detectOnlineStateService = binder.getService();
            detectOnlineStateService.setUpdateState(new DetectOnlineStateService.UpdateState() {
                @Override
                public void onUpdateState(List<ContactGroup> peopleList) {
                    for (ContactGroup contactGroup : peopleList) {
                        List<People> peopleOnline = new ArrayList<>();
                        List<People> peopleOffline = new ArrayList<>();
                        for (People people : contactGroup.getListPeople()) {
                            long period = 0;

                            if (people.getStatus() != null) {
                                long lastUpdateOnline = people.getStatus().getOnlinestamp();
                                long currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
                                period = currentTime - (lastUpdateOnline);
                            }

                            if (period <= 60 * 1000) {
                                peopleOnline.add(people);
                            } else peopleOffline.add(people);
                        }
                        contactGroup.getListPeople().clear();
                        contactGroup.getListPeople().addAll(peopleOnline);
                        contactGroup.getListPeople().addAll(peopleOffline);
                    }

                    contactFragment.showContactList(peopleList);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            detectOnlineStateService = null;
        }
    };

    public ContactFragPresenter(ContactFragContract.View view, DataManagement dataSource) {
        this.contactFragment = view;
        this.dataManagement = dataSource;

    }

    @Override
    public void start() {
        loadContactList(true);
        startService(contactFragment.getContextView());
    }

    @Override
    public void refreshOnlineStatus() {
    }

    @Override
    public void stopUpdateStateOnline() {
        unBindService(contactFragment.getContextView());
        contactFragment.getContextView().stopService(new Intent(contactFragment.getContextView(), DetectOnlineStateService.class));
    }

    @Override
    public void delete(String groupName, People people) {
        ContactDao.getInstance(contactFragment.getContextView()).delete(MapUtils.map6(people));
        dataManagement.deleteContact(groupName, people.getUid());
        loadContactList(true);
    }

    @Override
    public void block(String groupName, People people) {

    }

    private void startService(Context activity) {
        Intent intent = new Intent(activity, DetectOnlineStateService.class);
        activity.startService(intent);
        activity.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

    }

    public void unBindService(Context activity){
        activity.unbindService(serviceConnection);
    }

    @Override
    public void loadContactList(boolean shouldLoadLocal) {
        contactFragment.showWaitingProcess();
        sharedpreferences = contactFragment.getContextView()
                .getSharedPreferences(KEY_SHARE_PREFERENCE_CONTACT, Context.MODE_PRIVATE);
//        boolean isSaveLocal = sharedpreferences.getBoolean(KEY_SAVE_LOCAL, false);
        if (!shouldLoadLocal) {
            dataManagement.getContactList(new DataSource.LoadDataListener() {
                @Override
                public void onDataLoaded(Object data) {
                    List<ContactGroup> contactGroupList = (List<ContactGroup>) data;
                    contactFragment.showContactList(contactGroupList);
                    addAllContact(contactGroupList);
                }

                @Override
                public void onDataNotAvailable() {
                    contactFragment.stopWaitingProcess();
                    contactFragment.showContactList(new ArrayList<ContactGroup>());
                    contactFragment.showEmptyMsg();
                }

                @Override
                public void onError(String cause) {
                    contactFragment.stopWaitingProcess();
                    contactFragment.showBackgroundMsg(cause);
                }
            });
        } else {
            List<ContactGroup> contactGroups = MapUtils.map4(
                    ContactDao.getInstance(contactFragment.getContextView()).getAllContact());
            if(contactGroups == null || contactGroups.isEmpty()){
                loadContactList(false);
            }else {
                contactFragment.showContactList(contactGroups);
                if (detectOnlineStateService != null) detectOnlineStateService.loadStateOnline();
            }
        }
    }

    public void addAllContact(List<ContactGroup> contactGroups) {
        if(contactGroups == null || contactGroups.isEmpty()){
            return;
        }
        ContactDao.getInstance(contactFragment.getContextView()).insert(MapUtils.map3(contactGroups));
//        sharedpreferences.edit().putBoolean(KEY_SAVE_LOCAL, true).commit();
    }
}
