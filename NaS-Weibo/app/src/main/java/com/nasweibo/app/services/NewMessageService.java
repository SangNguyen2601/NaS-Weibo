package com.nasweibo.app.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nasweibo.app.R;
import com.nasweibo.app.chat.ChatActivity;
import com.nasweibo.app.data.Conversation;
import com.nasweibo.app.data.Message;
import com.nasweibo.app.data.User;
import com.nasweibo.app.util.AccountUtils;
import com.nasweibo.app.util.Config;
import com.nasweibo.app.util.Constant;
import com.nasweibo.app.util.DateUtils;
import com.nasweibo.app.util.ImageUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class NewMessageService extends Service {
    public static String TAG = "NewMessageService";
    private NotificationCompat.Builder notification;
    public static int ID_NOTI = 9;
    boolean isShowNoti = true;

    NotificationManager notificationManager;
    RemoteViews simpleContentView;

    private final IBinder mBinder = new LocalBinder();
    private DatabaseReference mDatabase;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        String myuid = AccountUtils.getUID(getApplicationContext());
        mDatabase.child("conversation").child(myuid)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.getValue() != null) {
                            Message lastMessage = dataSnapshot.getValue(Message.class);
                            String conversationID = dataSnapshot.getKey();

                            updateMsgStatus(conversationID);
                            final Conversation conversation = new Conversation();
                            conversation.setId(conversationID);
                            conversation.setLastMessage(lastMessage);

                            mDatabase.child("users").child(lastMessage.getIdReceiver())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getValue() != null) {
                                                User friend = dataSnapshot.getValue(User.class);
                                                conversation.setFriend(friend);
                                                if (!Config.isAppVisible()) {
                                                    createNotification(conversation);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        return START_STICKY;
    }

    @SuppressLint("NewApi")
    public void createNotification(Conversation conversation) {
        Message message = conversation.getLastMessage();
        User friend = conversation.getFriend();

        simpleContentView = new RemoteViews(getApplicationContext().getPackageName(),
                R.layout.notification_new_message);
        Intent intent = new Intent(NewMessageService.this, ChatActivity.class);
        intent.putExtra("friend", conversation.getFriend());
        PendingIntent contentIntent = PendingIntent.getActivity(NewMessageService.this, 0, intent, 0);

        notification = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(contentIntent)
                .setContentTitle(friend.getName())
                .setDefaults(Notification.DEFAULT_ALL)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true);

        notification.setContent(simpleContentView);

        new GetBitmapFromUrl().execute(friend.getAvatar());
        simpleContentView.setTextViewText(R.id.txt_user_name, friend.getName());
        simpleContentView.setTextViewText(R.id.txt_message, message.getText());
        String time = DateUtils.formatTimeWithMarker(message.getTimestamp());
        simpleContentView.setTextViewText(R.id.tv_time, time);

        notificationManager.notify(ID_NOTI, notification.build());

    }

    private void updateMsgStatus(final String conversationID) {
        mDatabase.child("message").child(conversationID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String msgID = dataSnapshot.getKey();
                        if (dataSnapshot.getValue() != null) {
                            Message message = dataSnapshot.getValue(Message.class);
                            if(message != null){
                                if(!message.getIdSender().equals(AccountUtils.getUID(getApplicationContext()))){
                                    if(Message.SENT_STATUS.equals(message.getStatus())){
                                        mDatabase.child("message").child(conversationID).child(msgID)
                                                .child("status").setValue(Message.RECEIVED_STATUS);
                                    }

                                }
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent();
        intent.setAction(Constant.APP_KILL_ACTION);
        sendBroadcast(intent);
    }

    public void setShowNoti(boolean isShowNoti) {
        Log.d("Noti_NOTIFICATION", " isShowNoti "+this.isShowNoti+"=> " +isShowNoti);
        this.isShowNoti = isShowNoti;
    }

    class GetBitmapFromUrl extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            InputStream in;
            try {

                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                in = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(in);
                return myBitmap;
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                bitmap = ImageUtils.getRoundedCornerBitmap(bitmap, 9);
                simpleContentView.setImageViewBitmap(R.id.img_avatar, bitmap);
                notificationManager.notify(ID_NOTI, notification.build());

            } else {
                Bitmap icon = BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher);

                simpleContentView.setImageViewBitmap(R.id.img_avatar, icon);
                notificationManager.notify(ID_NOTI, notification.build());

            }
        }
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public NewMessageService getService() {
            // Return this instance of LocalService so clients can call public methods
            return NewMessageService.this;
        }
    }
}
