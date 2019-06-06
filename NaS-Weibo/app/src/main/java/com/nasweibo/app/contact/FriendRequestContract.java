package com.nasweibo.app.contact;

import com.nasweibo.app.BasePresenter;
import com.nasweibo.app.BaseView;
import com.nasweibo.app.data.User;


public interface FriendRequestContract {

    interface View extends BaseView<Presenter>{

        void showSearchFragment();

        void showPreviewContact(User friend);

    }

    interface Presenter extends BasePresenter{
        void doNext();

        void doBack();
    }
}
