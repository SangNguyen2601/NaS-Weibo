package com.nasweibo.app.contact;

import android.content.Context;

import com.nasweibo.app.BasePresenter;
import com.nasweibo.app.BaseView;
import com.nasweibo.app.data.ContactGroup;
import com.nasweibo.app.data.People;

import java.util.List;


public interface ContactFragContract {

    interface View extends BaseView<Presenter> {
        void showWaitingProcess();

        void stopWaitingProcess();

        void showBackgroundMsg(String msg);

        void showEmptyMsg();

        void showContactList(List<ContactGroup> contactGroups);

        Context getContextView();
    }

    interface Presenter extends BasePresenter {
        void loadContactList(boolean shouldLoadLocal);

        void refreshOnlineStatus();

        void stopUpdateStateOnline();

        void delete(String groupName, People people);

        void block(String groupName, People people);
    }
}
