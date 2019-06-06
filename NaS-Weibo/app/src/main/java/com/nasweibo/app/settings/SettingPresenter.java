package com.nasweibo.app.settings;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.nasweibo.app.data.User;
import com.nasweibo.app.ui.BaseTabFragment;
import com.nasweibo.app.util.AccountUtils;

import org.json.JSONException;
import org.json.JSONObject;


public class SettingPresenter implements SettingContract.Presenter {

    private final SettingContract.View settingFragment;
    private Context mContext;

    public SettingPresenter(SettingContract.View settingFragment) {
        this.settingFragment = settingFragment;

        if (settingFragment instanceof BaseTabFragment) {
            mContext = ((Fragment) settingFragment).getContext();
        } else if (settingFragment instanceof AppCompatActivity) {
            mContext = (Context) settingFragment;
        } else {
            throw new RuntimeException("Cannot cast DishesContract.View to Activity or Fragment");
        }
    }

    @Override
    public void start() {

    }

    @Override
    public User getUserProfile() {
        User profile = new User();
        String name = "";
        String email = "";
        String photoUrl = "";

        JSONObject user = AccountUtils.getUserProfile(mContext);
        if(user != null){
            try {
                name = user.getString(AccountUtils.USER_NAME);
                email = user.getString(AccountUtils.USER_EMAIL);
                photoUrl = user.getString(AccountUtils.USER_AVATAR);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        profile.setName(name);
        profile.setEmail(email);
        profile.setAvatar(photoUrl);

        return profile;
    }

    @Override
    public void refreshUserProfile() {

    }

    @Override
    public void logout() {
        FirebaseAuth.getInstance().signOut();
    }

    @Override
    public void deleteUserProfile() {
        AccountUtils.setUserValue(mContext, AccountUtils.USER_AVATAR, "");
        AccountUtils.setUserValue(mContext, AccountUtils.USER_EMAIL, "");
        AccountUtils.setUserValue(mContext, AccountUtils.USER_NAME, "");
    }
}
