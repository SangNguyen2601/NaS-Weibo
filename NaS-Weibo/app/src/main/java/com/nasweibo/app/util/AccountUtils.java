package com.nasweibo.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.json.JSONException;
import org.json.JSONObject;
import static com.nasweibo.app.util.LogUtils.makeLogTag;

public class AccountUtils {

    public static final String TAG = makeLogTag(AccountUtils.class);

    private static final String PREF_FACEBOOK_ACCOUNT = "facebook_account";
    public static final String FACEBOOK_ACC = "facebook";
    public static final String GOOGLE_ACC = "google";

    private static final String PREF_USER_PROFILE = "user_profile";
    private static final String ACCOUNT_ACTIVE = "account_active";
    public static final String ACCOUNT_TYPE = "account_type";

    public static final String USER_AVATAR = "avatar";
    public static final String USER_NAME = "name";
    public static final String USER_EMAIL = "email";

    public static final String FIREBASE_UID = "firebase_uid";

    private static SharedPreferences getSharedPreferences(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static JSONObject getUserProfile(Context context){
        JSONObject user = null;
        try{
            SharedPreferences sp = getSharedPreferences(context);
            String json = sp.getString(PREF_USER_PROFILE, "");
            if(!json.equals("")){
                user = new JSONObject(json);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return user;
    }

    public static boolean setUserValue(Context context, String key, String value){
        if(key == null || key.equals("")){
            return false;
        }

        try{
            SharedPreferences sp = getSharedPreferences(context);
            JSONObject user = getUserProfile(context);
            if(user == null){
                user = new JSONObject();
            }
            user.put(key, value);
            sp.edit().putString(PREF_USER_PROFILE, user.toString()).apply();
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addSettingValue(Context context, String key, String value){
        if(key == null || key.equals("")){
            return false;
        }

        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putString(key, value).apply();
        return true;
    }

    public static void saveUID(Context context, String uid){
        if(context == null ){
            return ;
        }

        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putString(FIREBASE_UID, uid).apply();
    }

    public static String getUID(Context context){
        if(context == null){
            return "";
        }

        SharedPreferences sp = getSharedPreferences(context);
        return sp.getString(FIREBASE_UID, "");
    }
}
