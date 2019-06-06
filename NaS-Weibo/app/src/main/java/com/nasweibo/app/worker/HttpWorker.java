package com.nasweibo.app.worker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.nasweibo.app.util.AccountUtils;
import com.nasweibo.app.util.ImageUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;



public class HttpWorker {

    public static void downloadAvatarOnline(final Context context, final String url){
        if(url == null || "".equals(url)){
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL avatar = new URL(url);
                    Bitmap bitmap = BitmapFactory.decodeStream(avatar.openConnection().getInputStream());
                    String sAvatar = ImageUtils.encodeBase64(bitmap);
                    AccountUtils.setUserValue(context, AccountUtils.USER_AVATAR, sAvatar);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
