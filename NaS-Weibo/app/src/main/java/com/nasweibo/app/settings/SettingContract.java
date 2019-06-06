package com.nasweibo.app.settings;

import com.nasweibo.app.BasePresenter;
import com.nasweibo.app.BaseView;
import com.nasweibo.app.data.User;


public interface SettingContract {

    interface View extends BaseView<Presenter> {

        void showProfile(User profile);

    }

    interface Presenter extends BasePresenter {
        User getUserProfile();

        void refreshUserProfile();

        void logout();

        void deleteUserProfile();
    }
}
