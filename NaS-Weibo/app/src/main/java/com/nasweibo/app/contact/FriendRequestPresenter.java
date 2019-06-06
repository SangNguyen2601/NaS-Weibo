package com.nasweibo.app.contact;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;


public class FriendRequestPresenter implements FriendRequestContract.Presenter {

    private FriendRequestContract.View friendRequestActivity;
    private Context mContext;

    public FriendRequestPresenter(FriendRequestContract.View view){
        this.friendRequestActivity = view;

        if(view instanceof AppCompatActivity){
            mContext = (Context) view;
        }else {
            throw new RuntimeException("Cannot cast DishesContract.View to Activity or Fragment");
        }
    }

    @Override
    public void start() {
        friendRequestActivity.showSearchFragment();
    }

    @Override
    public void doNext() {

    }

    @Override
    public void doBack() {

    }
}
