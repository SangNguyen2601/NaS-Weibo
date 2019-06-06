package com.nasweibo.app.welcome;

import android.content.Context;



public class IntroFragment extends WelcomeFragment implements WelcomeActivity.WelcomeContent{
    @Override
    public boolean shouldDisplay(Context context) {
        return false;
    }
}
