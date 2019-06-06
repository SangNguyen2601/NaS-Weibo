package com.nasweibo.app.util;

import com.nasweibo.app.MainActivity;
import com.nasweibo.app.chat.ChatActivity;



public class Config {

    public static final boolean DEBUG = false;

    public static boolean isAppVisible(){
        if(MainActivity.checkVisible()|| ChatActivity.checkVisible()){
            return true;
        }else {
            return false;
        }
    }
}
