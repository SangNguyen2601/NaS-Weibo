package com.nasweibo.app.chat;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nasweibo.app.R;
import com.nasweibo.app.data.Message;
import com.nasweibo.app.data.User;
import com.nasweibo.app.services.NewMessageService;
import com.nasweibo.app.services.PushStateOnlineService;
import com.nasweibo.app.ui.widget.recyclerview.PhotoViewer;
import com.nasweibo.app.util.AccountUtils;
import com.nasweibo.app.util.DateUtils;
import com.nasweibo.app.util.FileUtils;
import com.nasweibo.app.util.ImageUtils;
import com.nasweibo.app.util.UrlPreviewInfo;
import com.nasweibo.app.util.WebUtils;
import com.thefinestartist.finestwebview.FinestWebView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.TimeZone;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

import static com.nasweibo.app.util.LogUtils.LOGD;


public class ChatActivity extends AppCompatActivity {

    private static final String LOG_TAG = ChatActivity.class.getSimpleName();

    private static final int STATE_NORMAL = 0;
    private static final int STATE_EDIT = 1;

    private static final String STATE_CHANNEL_URL = "STATE_CHANNEL_URL";
    private static final int INTENT_REQUEST_CHOOSE_MEDIA = 301;
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 13;
    private NewMessageService newMessageService;
    private InputMethodManager mIMM;

    private RelativeLayout mRootLayout;
    private RecyclerView mRecyclerView;
    private ChatAdapter mChatAdapter;
    private LinearLayoutManager mLayoutManager;
    private EmojiconEditText mMessageEditText;
    private Button mMessageSendButton;
    private ImageButton mUploadFileButton;
    private View mCurrentEventLayout;
    private TextView mCurrentEventText;

    //    private GroupChannel mChannel;
    private String mChannelUrl = "";
//    private PreviousMessageListQuery mPrevMessageListQuery;

    private boolean mIsTyping;

    private int mCurrentState = STATE_NORMAL;
//    private BaseMessage mEditingMessage = null;

    /**
     * To create an instance of this fragment, a Channel URL should be required.
     */
//    public static GroupChatFragment newInstance(@NonNull String channelUrl) {
//        GroupChatFragment fragment = new GroupChatFragment();
//
//        Bundle args = new Bundle();
//        args.putString(GroupChannelListFragment.EXTRA_GROUP_CHANNEL_URL, channelUrl);
//        fragment.setArguments(args);
//
//        return fragment;
//    }

    private DatabaseReference mDatabase;
    private User chatFriend;
    private List<Message> historyMessageList = new ArrayList<>();
    private String currentConversation;
    private CircularProgressView progressView;
    private LinearLayoutManager linearLayoutManager;
    private ImageView emojiBtn;
    EmojIconActions emojIcon;
    private boolean isBound = false;

    private static boolean isVisible = false;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_chat);
        mRootLayout = findViewById(R.id.layout_chat_root);
        emojiBtn = findViewById(R.id.emoji_btn);

        progressView = findViewById(R.id.load_history_progress);
        showWaitingProcess();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            chatFriend = (User) bundle.getSerializable("friend");
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(chatFriend.getName());
            }
            loadChatHistory(chatFriend.getUid());
        }

        mIMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        mFileProgressHandlerMap = new HashMap<>();

//        if (savedInstanceState != null) {
//            // Get channel URL from saved state.
//            mChannelUrl = savedInstanceState.getString(STATE_CHANNEL_URL);
//        } else {
//            // Get channel URL from GroupChannelListFragment.
//            mChannelUrl = getArguments().getString(GroupChannelListFragment.EXTRA_GROUP_CHANNEL_URL);
//        }

        Log.d(LOG_TAG, mChannelUrl);

        mChatAdapter = new ChatAdapter(this, historyMessageList);
        setUpChatListAdapter();

        // Load messages from cache.
//        mChatAdapter.load(mChannelUrl);

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView = findViewById(R.id.recycler_group_chat);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mCurrentEventLayout = findViewById(R.id.layout_group_chat_current_event);
        mCurrentEventText = findViewById(R.id.text_group_chat_current_event);

        mMessageEditText = findViewById(R.id.edittext_group_chat_message);
        mMessageSendButton = findViewById(R.id.button_group_chat_send);
        mUploadFileButton = findViewById(R.id.btn_attach_file);

        emojIcon = new EmojIconActions(this, mRootLayout, mMessageEditText, emojiBtn);
        emojIcon.setUseSystemEmoji(true);
        emojIcon.setIconsIds(R.drawable.ic_action_keyboard, R.drawable.smiley);
        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                Log.e("ChatActivity", "Keyboard opened!");

            }

            @Override
            public void onKeyboardClose() {
                Log.e("ChatActivity", "Keyboard closed");
            }
        });

        emojiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emojIcon.ShowEmojIcon();
            }
        });

        mMessageSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput = mMessageEditText.getText().toString();
                if (userInput.length() > 0) {
                    sendUserMessage(userInput);
                    mMessageEditText.setText("");
                }
            }
        });

        mUploadFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestMedia();
            }
        });
        mIsTyping = false;
        setUpRecyclerView();
    }

    private void updateMsgSeenStatus() {

    }

    public void showWaitingProcess() {
        progressView.setVisibility(View.VISIBLE);
        progressView.startAnimation();
    }

    public void stopWaitingProcess() {
        progressView.stopAnimation();
        progressView.setVisibility(View.INVISIBLE);
    }

    private void loadChatHistory(final String friendUid) {
        if (friendUid == null || "".equals(friendUid)) {
            return;
        }

        final String myUid = AccountUtils.getUID(this);
        mDatabase.child("conversation").child(myUid).orderByChild("idReceiver").equalTo(friendUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                currentConversation = snapshot.getKey();
//                                fetchListMessage(currentConversation);
                                listenForNewMessage(currentConversation);
                                break;
                            }
                        } else {
                            //create new conversation
                            final Message chat = new Message();
                            chat.setIdReceiver(friendUid);
                            chat.setIdSender(myUid);
                            chat.setType(Message.TEXT_TYPE);
                            chat.setValue("");
                            chat.setStatus("");
                            chat.setTimestamp(System.currentTimeMillis());


                            mDatabase.child("conversation").child(friendUid).orderByChild("idReceiver")
                                    .equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.getValue() != null) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            currentConversation = snapshot.getKey();
                                            break;
                                        }
                                        if (currentConversation != null) {
                                            mDatabase.child("conversation").child(myUid).child(currentConversation)
                                                    .setValue(chat);

                                            listenForNewMessage(currentConversation);
                                        } else {
                                            //Create a new conversation for both me and friend
                                            currentConversation = mDatabase.child("conversation").child(myUid).push().getKey();
                                            mDatabase.child("conversation").child(myUid).child(currentConversation)
                                                    .setValue(chat, new DatabaseReference.CompletionListener() {
                                                        @Override
                                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                            if (databaseError != null) {
                                                                Toast.makeText(getApplicationContext(), R.string.start_conversation_failed,
                                                                        Toast.LENGTH_LONG).show();
                                                            }
                                                            stopWaitingProcess();
                                                        }
                                                    });

                                            chat.setIdReceiver(myUid);
                                            chat.setIdSender(friendUid);
                                            mDatabase.child("conversation").child(friendUid).child(currentConversation)
                                                    .setValue(chat);

                                            listenForNewMessage(currentConversation);
                                        }

                                    } else {
                                        //Create a new conversation for both me and friend
                                        currentConversation = mDatabase.child("conversation").child(myUid).push().getKey();
                                        mDatabase.child("conversation").child(myUid).child(currentConversation)
                                                .setValue(chat, new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                        if (databaseError != null) {
                                                            Toast.makeText(getApplicationContext(), R.string.start_conversation_failed,
                                                                    Toast.LENGTH_LONG).show();
                                                        }
                                                        stopWaitingProcess();
                                                    }
                                                });

                                        chat.setIdReceiver(myUid);
                                        chat.setIdSender(friendUid);
                                        mDatabase.child("conversation").child(friendUid).child(currentConversation)
                                                .setValue(chat);

                                        listenForNewMessage(currentConversation);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        stopWaitingProcess();
                    }
                });
    }

    private void listenForNewMessage(String conversationID) {
        stopWaitingProcess();
        mDatabase.child("message").child(conversationID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String msgID = dataSnapshot.getKey();
                        if (dataSnapshot.getValue() != null) {
                            Message message = dataSnapshot.getValue(Message.class);
                            if (message.isTempMessage() && message.getIdSender()
                                    .equals(AccountUtils.getUID(getApplicationContext()))) {
                                for (int i = historyMessageList.size() - 1; i >= 0; i--) {
                                    Message tmpMessage = historyMessageList.get(i);
                                    if (tmpMessage.getTimestamp() == message.getTimestamp()) {
                                        historyMessageList.set(i, message);
                                        mChatAdapter.notifyDataSetChanged();
                                        return;
                                    }
                                }
                            } else {
                                mChatAdapter.addFirst(message);
                            }

                            if (!message.getIdSender().equals(AccountUtils.getUID(getApplicationContext()))) {
                                if (!Message.SEEN_STATUS.equals(message.getStatus())) {
                                    mDatabase.child("message").child(currentConversation).child(msgID)
                                            .child("status").setValue(Message.SEEN_STATUS);
                                }
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.getValue() != null) {
                            Message message = dataSnapshot.getValue(Message.class);
                            for (int i = historyMessageList.size() - 1; i >= 0; i--) {
                                Message tmpMessage = historyMessageList.get(i);
                                if (tmpMessage.getTimestamp() == message.getTimestamp()) {
                                    historyMessageList.set(i, message);
                                    mChatAdapter.notifyDataSetChanged();
                                    return;
                                }
                            }
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
                        stopWaitingProcess();
                    }
                });

    }


//    private void refresh() {
//        if (mChannel == null) {
//            GroupChannel.getChannel(mChannelUrl, new GroupChannel.GroupChannelGetHandler() {
//                @Override
//                public void onResult(GroupChannel groupChannel, SendBirdException e) {
//                    if (e != null) {
//                        // Error!
//                        e.printStackTrace();
//                        return;
//                    }
//
//                    mChannel = groupChannel;
//                    mChatAdapter.setChannel(mChannel);
//                    mChatAdapter.loadLatestMessages(CHANNEL_LIST_LIMIT, new BaseChannel.GetMessagesHandler() {
//                        @Override
//                        public void onResult(List<BaseMessage> list, SendBirdException e) {
//                            mChatAdapter.markAllMessagesAsRead();
//                        }
//                    });
//                    updateActionBarTitle();
//                }
//            });
//        } else {
//            mChannel.refresh(new GroupChannel.GroupChannelRefreshHandler() {
//                @Override
//                public void onResult(SendBirdException e) {
//                    if (e != null) {
//                        // Error!
//                        e.printStackTrace();
//                        return;
//                    }
//
//                    mChatAdapter.loadLatestMessages(CHANNEL_LIST_LIMIT, new BaseChannel.GetMessagesHandler() {
//                        @Override
//                        public void onResult(List<BaseMessage> list, SendBirdException e) {
//                            mChatAdapter.markAllMessagesAsRead();
//                        }
//                    });
//                    updateActionBarTitle();
//                }
//            });
//        }
//    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NewMessageService.LocalBinder binder = (NewMessageService.LocalBinder) service;
            newMessageService = binder.getService();
            Log.d("Noti_ChatActivity", "setShowNoti()");
            newMessageService.setShowNoti(false);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    public static boolean checkVisible() {
        return isVisible;
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent2 = new Intent(this, NewMessageService.class);
        startService(intent2);
        bindService(intent2, serviceConnection, Context.BIND_AUTO_CREATE);
        isBound = true;

        isVisible = true;
        Log.d("Noti_ChatActivity", "isVisible = " + isVisible);
    }

    @Override
    public void onResume() {
        super.onResume();

        mChatAdapter.setContext(this); // Glide bug fix (java.lang.IllegalArgumentException: You cannot start a load for a destroyed activity)

        // Gets channel from URL user requested

//        Log.d(LOG_TAG, mChannelUrl);

//        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
//            @Override
//            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
//                if (baseChannel.getUrl().equals(mChannelUrl)) {
//                    mChatAdapter.markAllMessagesAsRead();
//                    // Add new message to view
//                    mChatAdapter.addFirst(baseMessage);
//                }
//            }
//
//            @Override
//            public void onMessageDeleted(BaseChannel baseChannel, long msgId) {
//                super.onMessageDeleted(baseChannel, msgId);
//                if (baseChannel.getUrl().equals(mChannelUrl)) {
//                    mChatAdapter.delete(msgId);
//                }
//            }
//
//            @Override
//            public void onMessageUpdated(BaseChannel channel, BaseMessage message) {
//                super.onMessageUpdated(channel, message);
//                if (channel.getUrl().equals(mChannelUrl)) {
//                    mChatAdapter.update(message);
//                }
//            }
//
//            @Override
//            public void onReadReceiptUpdated(GroupChannel channel) {
//                if (channel.getUrl().equals(mChannelUrl)) {
//                    mChatAdapter.notifyDataSetChanged();
//                }
//            }
//
//            @Override
//            public void onTypingStatusUpdated(GroupChannel channel) {
//                if (channel.getUrl().equals(mChannelUrl)) {
//                    List<Member> typingUsers = channel.getTypingMembers();
//                    displayTyping(typingUsers);
//                }
//            }
//
//        });
    }

    @Override
    public void onPause() {
//        setTypingStatus(false);
//        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (newMessageService != null) {
            Log.d("Noti_ChatActivity", "setShowNoti()");
            newMessageService.setShowNoti(true);
        }

        isVisible = false;
        Log.d("Noti_ChatActivity", "isVisible = " + isVisible);

    }

    @Override
    public void onDestroy() {
        // Save messages to cache.
//        ConnectionManager.removeConnectionManagementHandler(CONNECTION_HANDLER_ID);
//        mChatAdapter.save();
        super.onDestroy();
        stopService(new Intent(this, PushStateOnlineService.class));
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_CHANNEL_URL, mChannelUrl);

        super.onSaveInstanceState(outState);
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.menu_group_chat, menu);
//        super.onCreateOptionsMenu(menu, inflater);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

//        if (id == R.id.action_group_channel_invite) {
//            Intent intent = new Intent(getActivity(), InviteMemberActivity.class);
//            intent.putExtra(EXTRA_CHANNEL_URL, mChannelUrl);
//            startActivity(intent);
//            return true;
//        } else if (id == R.id.action_group_channel_view_members) {
//            Intent intent = new Intent(getActivity(), MemberListActivity.class);
//            intent.putExtra(EXTRA_CHANNEL_URL, mChannelUrl);
//            startActivity(intent);
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Set this as true to restore background connection management.
//        SendBird.setAutoBackgroundDetection(true);

        if (requestCode == INTENT_REQUEST_CHOOSE_MEDIA && resultCode == Activity.RESULT_OK) {
            // If user has successfully chosen the image, show a dialog to confirm upload.
            if (data == null) {
                Log.d(LOG_TAG, "data is null!");
                return;
            }

            sendFileWithThumbnail(data.getData());
        }
    }

    private void setUpRecyclerView() {
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mChatAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (mLayoutManager.findLastVisibleItemPosition() == mChatAdapter.getItemCount() - 1) {
//                    mChatAdapter.loadPreviousMessages(CHANNEL_LIST_LIMIT, null);
                }
            }
        });
    }

    private void setUpChatListAdapter() {
        mChatAdapter.setItemClickListener(new ChatAdapter.OnItemClickListener() {
            @Override
            public void onUserMessageItemClick(Message message) {
                if (Message.URL_TYPE.equals(message.getType())) {
                    List<String> urls = WebUtils.extractUrls(message.getText());
                    if (urls.size() > 0) {
                        new FinestWebView.Builder(getApplicationContext()).show(urls.get(0));
                    }
                }
            }

            @Override
            public void onFileMessageItemClick(Message message) {
                // Load media chooser and remove the failed message from list.
//                if (mChatAdapter.isFailedMessage(message)) {
//                    retryFailedMessage(message);
//                    return;
//                }

                // Message is sending. Do nothing on click event.
//                if (mChatAdapter.isTempMessage(message)) {
//                    return;
//                }


                onFileMessageClicked(message);
            }
        });

//        mChatAdapter.setItemLongClickListener(new ChatAdapter.OnItemLongClickListener() {
//            @Override
//            public void onUserMessageItemLongClick(UserMessage message, int position) {
//                showMessageOptionsDialog(message, position);
//            }
//
//            @Override
//            public void onFileMessageItemLongClick(FileMessage message) {
//            }
//
//            @Override
//            public void onAdminMessageItemLongClick(AdminMessage message) {
//            }
//        });
    }

//    private void showMessageOptionsDialog(final BaseMessage message, final int position) {
//        String[] options = new String[] { "Edit message", "Delete message" };
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setItems(options, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                if (which == 0) {
//                    setState(STATE_EDIT, message, position);
//                } else if (which == 1) {
//                    deleteMessage(message);
//                }
//            }
//        });
//        builder.create().show();
//    }

//    private void setState(int state, Message editingMessage, final int position) {
//        switch (state) {
//            case STATE_NORMAL:
//                mCurrentState = STATE_NORMAL;
//                mEditingMessage = null;
//
//                mUploadFileButton.setVisibility(View.VISIBLE);
//                mMessageSendButton.setText("SEND");
//                mMessageEditText.setText("");
//                break;
//
//            case STATE_EDIT:
//                mCurrentState = STATE_EDIT;
//                mEditingMessage = editingMessage;
//
//                mUploadFileButton.setVisibility(View.GONE);
//                mMessageSendButton.setText("SAVE");
//                String messageString = ((UserMessage)editingMessage).getMessage();
//                if (messageString == null) {
//                    messageString = "";
//                }
//                mMessageEditText.setText(messageString);
//                if (messageString.length() > 0) {
//                    mMessageEditText.setSelection(0, messageString.length());
//                }
//
//                mMessageEditText.requestFocus();
//                mMessageEditText.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        mIMM.showSoftInput(mMessageEditText, 0);
//
//                        mRecyclerView.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                mRecyclerView.scrollToPosition(position);
//                            }
//                        }, 500);
//                    }
//                }, 100);
//                break;
//        }
//    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        ((GroupChannelActivity)context).setOnBackPressedListener(new GroupChannelActivity.onBackPressedListener() {
//            @Override
//            public boolean onBack() {
//                if (mCurrentState == STATE_EDIT) {
//                    setState(STATE_NORMAL, null, -1);
//                    return true;
//                }
//
//                mIMM.hideSoftInputFromWindow(mMessageEditText.getWindowToken(), 0);
//                return false;
//            }
//        });
//    }

//    private void retryFailedMessage(final BaseMessage message) {
//        new AlertDialog.Builder(this)
//                .setMessage("Retry?")
//                .setPositiveButton("Resend", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (which == DialogInterface.BUTTON_POSITIVE) {
//                            if (message instanceof UserMessage) {
//                                String userInput = ((UserMessage) message).getMessage();
//                                sendUserMessage(userInput);
//                            } else if (message instanceof FileMessage) {
//                                Uri uri = mChatAdapter.getTempFileMessageUri(message);
//                                sendFileWithThumbnail(uri);
//                            }
//                            mChatAdapter.removeFailedMessage(message);
//                        }
//                    }
//                })
//                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (which == DialogInterface.BUTTON_NEGATIVE) {
//                            mChatAdapter.removeFailedMessage(message);
//                        }
//                    }
//                }).show();
//    }

    /**
     * Display which users are typing.
     * If more than two users are currently typing, this will state that "multiple users" are typing.
     * <p>
     * //     * @param typingUsers The list of currently typing users.
     */
//    private void displayTyping(List<Member> typingUsers) {
//
//        if (typingUsers.size() > 0) {
//            mCurrentEventLayout.setVisibility(View.VISIBLE);
//            String string;
//
//            if (typingUsers.size() == 1) {
//                string = typingUsers.get(0).getNickname() + " is typing";
//            } else if (typingUsers.size() == 2) {
//                string = typingUsers.get(0).getNickname() + " " + typingUsers.get(1).getNickname() + " is typing";
//            } else {
//                string = "Multiple users are typing";
//            }
//            mCurrentEventText.setText(string);
//        } else {
//            mCurrentEventLayout.setVisibility(View.GONE);
//        }
//    }
    private void requestMedia() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // If storage permissions are not granted, request permissions at run-time,
            // as per < API 23 guidelines.
            requestStoragePermissions();
        } else {
            Intent intent = new Intent();

            // Pick images or videos
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent.setType("*/*");
                String[] mimeTypes = {"image/*", "video/*"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            } else {
                intent.setType("image/* video/*");
            }

            intent.setAction(Intent.ACTION_GET_CONTENT);

            // Always show the chooser (if there are multiple options available)
            startActivityForResult(Intent.createChooser(intent, "Select Media"), INTENT_REQUEST_CHOOSE_MEDIA);

            // Set this as false to maintain connection
            // even when an external Activity is started.
//            SendBird.setAutoBackgroundDetection(false);
        }
    }

    private void requestStoragePermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Snackbar.make(mRootLayout, "Storage access permissions are required to upload/download files.",
                    Snackbar.LENGTH_LONG)
                    .setAction("Okay", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSION_WRITE_EXTERNAL_STORAGE);
                            }

                        }
                    })
                    .show();
        } else {
            // Permission has not been granted yet. Request it directly.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    private void onFileMessageClicked(Message message) {
        String type = message.getType().toLowerCase();
        if (type.startsWith("image")) {
            Intent intent = new Intent(this, PhotoViewer.class);
            intent.putExtra("url", message.getValue());
            startActivity(intent);
        } else if (type.startsWith("video")) {

        } else {
//            showDownloadConfirmDialog(message);
        }
    }

//    private void showDownloadConfirmDialog(final FileMessage message) {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            // If storage permissions are not granted, request permissions at run-time,
//            // as per < API 23 guidelines.
//            requestStoragePermissions();
//        } else {
//            new AlertDialog.Builder(this)
//                    .setMessage("Download file?")
//                    .setPositiveButton("Download", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            if (which == DialogInterface.BUTTON_POSITIVE) {
//                                FileUtils.downloadFile(ChatActivity.this, message.getUrl(), message.getName());
//                            }
//                        }
//                    })
//                    .setNegativeButton("Cancel", null).show();
//        }
//
//    }

//    private void updateActionBarTitle() {
//        String title = "";
//
//        if(mChannel != null) {
//            title = TextUtils.getGroupChannelTitle(mChannel);
//        }

    // Set action bar title to name of channel
//        if (getActivity() != null) {
//            ((GroupChannelActivity) getActivity()).setActionBarTitle(title);
//        }
//    }

    private void sendUserMessageWithUrl(final String text, String url) {
        new WebUtils.UrlPreviewAsyncTask() {
            @Override
            protected void onPostExecute(UrlPreviewInfo info) {
                try {
                    // Sending a message with URL preview information and custom type.
                    String jsonString = info.toJsonString();

                    Message message = new Message();
                    message.setIdSender(AccountUtils.getUID(getApplicationContext()));
                    message.setIdReceiver(chatFriend.getUid());
                    message.setType(Message.URL_TYPE);
                    message.setValue(jsonString);
                    message.setText(text);
                    long currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
                    message.setTimestamp(currentTime);
                    message.setStatus(Message.SENT_STATUS);


                    String msgID = mDatabase.child("message").child(currentConversation).push().getKey();
                    mDatabase.child("message").child(currentConversation).child(msgID)
                            .setValue(message, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError == null) {
                                    }
                                }
                            });

                    //update last message for conversation
                    mDatabase.child("conversation").child(AccountUtils.getUID(getApplicationContext()))
                            .child(currentConversation)
                            .setValue(message);

                    message.setIdSender(chatFriend.getUid());
                    message.setIdReceiver(AccountUtils.getUID(getApplicationContext()));
                    mDatabase.child("conversation").child(chatFriend.getUid()).child(currentConversation)
                            .setValue(message);

                } catch (Exception e) {
                    // Sending a message without URL preview information.
//                    tempUserMessage = mChannel.sendUserMessage(text, handler);
                }
            }
        }.execute(url);
    }

    private void sendUserMessage(String text) {
        Message message = new Message();
        message.setIdSender(AccountUtils.getUID(this));
        message.setIdReceiver(chatFriend.getUid());
        message.setTempMessage(true);
        message.setType(Message.TEXT_TYPE);
        message.setText(text);
        long currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
        message.setTimestamp(currentTime);
        message.setStatus(Message.SENDING_STATUS);
        message.setId("tmp_" + currentTime);

        List<String> urls = WebUtils.extractUrls(text);
        if (urls.size() > 0) {
            mChatAdapter.addFirst(message);
            sendUserMessageWithUrl(text, urls.get(0));
            return;
        }

        sendMessageFirebase(message);
        // Display a user message to RecyclerView
        mChatAdapter.addFirst(message);
    }

    private void sendMessageFirebase(final Message message) {
        if (message == null) {
            return;
        }

        final String msgID = mDatabase.child("message").child(currentConversation).push().getKey();
        mDatabase.child("message").child(currentConversation).child(msgID)
                .setValue(message, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            message.setTempMessage(false);
                            message.setId(msgID);
                            message.setStatus(Message.SENT_STATUS);
                            mDatabase.child("message").child(currentConversation).child(msgID)
                                    .setValue(message);
                        }
                    }
                });

        message.setTempMessage(false);
        message.setId(msgID);
        //update last message for conversation
        mDatabase.child("conversation").child(AccountUtils.getUID(this)).child(currentConversation)
                .setValue(message);

        Message otherMsgUpdate = new Message();
        otherMsgUpdate.reverseSender(message);
        otherMsgUpdate.setTempMessage(false);
        mDatabase.child("conversation").child(chatFriend.getUid()).child(currentConversation)
                .setValue(otherMsgUpdate);
    }

    /**
     * Notify other users whether the current user is typing.
     *
     * @param typing Whether the user is currently typing.
     */
//    private void setTypingStatus(boolean typing) {
//        if (mChannel == null) {
//            return;
//        }
//
//        if (typing) {
//            mIsTyping = true;
//            mChannel.startTyping();
//        } else {
//            mIsTyping = false;
//            mChannel.endTyping();
//        }
//    }

    /**
     * Sends a File Message containing an image file.
     * Also requests thumbnails to be generated in specified sizes.
     *
     * @param uri The URI of the image, which in this case is received through an Intent request.
     */
    private void sendFileWithThumbnail(Uri uri) {
        try {
            Hashtable<String, Object> info = FileUtils.getFileInfo(this, uri);
            if (info == null) {
                Toast.makeText(this, "Extracting file information failed.", Toast.LENGTH_LONG).show();
                return;
            }

            final String path = (String) info.get("path");
            final File file = new File(path);
            final String name = file.getName();
            final String mime = (String) info.get("mime");
            final int size = (Integer) info.get("size");

            String pathResized = ImageUtils.resizeAndCompressImageBeforeSend(this, path, name);
            File fileResized = new File(pathResized);
            InputStream inputStream = new FileInputStream(fileResized);

            if (path.equals("")) {
                Toast.makeText(this, "File must be located in local storage.", Toast.LENGTH_LONG).show();
            } else {
                mChatAdapter.startProgressUploadFile();

                final Message message = new Message();
                message.setIdReceiver(chatFriend.getUid());
                message.setIdSender(AccountUtils.getUID(getApplicationContext()));
                message.setType(Message.IMAGE_TYPE);
                message.setTempMessage(true);
                final long msgTimestamp = DateUtils.getCurrentTimeMillisUTC();
                message.setTimestamp(msgTimestamp);
                message.setStatus(Message.SENT_STATUS);

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                StorageReference riversRef = storageRef.child("images/" + name + "_" + DateUtils.getCurrentTimeMillisUTC());

                UploadTask uploadTask = riversRef.putStream(inputStream);
                mChatAdapter.addFirst(message);
                // Register observers to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        LOGD("Firebase upload", "FAILED");
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        if (downloadUrl != null) {
                            LOGD("Firebase image upload", "SUCCESS with url = " + downloadUrl.toString());
                            mChatAdapter.deleteTempMessage(msgTimestamp);
                            message.setValue(downloadUrl.toString());
                            message.setTempMessage(false);
                            sendMessageFirebase(message);
                        }

                    }
                });
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
//
//    private void editMessage(final BaseMessage message, String editedMessage) {
//        mChannel.updateUserMessage(message.getMessageId(), editedMessage, null, null, new BaseChannel.UpdateUserMessageHandler() {
//            @Override
//            public void onUpdated(UserMessage userMessage, SendBirdException e) {
//                if (e != null) {
//                    // Error!
//                    Toast.makeText(getApplicationContext(), "Error " + e.getCode() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                mChatAdapter.loadLatestMessages(CHANNEL_LIST_LIMIT, new BaseChannel.GetMessagesHandler() {
//                    @Override
//                    public void onResult(List<BaseMessage> list, SendBirdException e) {
//                        mChatAdapter.markAllMessagesAsRead();
//                    }
//                });
//            }
//        });
//    }

    /**
     * Deletes a message within the channel.
     * Note that users can only delete messages sent by oneself.
     *
     * @param message The message to delete.
     */
//    private void deleteMessage(final BaseMessage message) {
//        mChannel.deleteMessage(message, new BaseChannel.DeleteMessageHandler() {
//            @Override
//            public void onResult(SendBirdException e) {
//                if (e != null) {
//                    // Error!
//                    Toast.makeText(getApplicationContext(), "Error " + e.getCode() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                mChatAdapter.loadLatestMessages(CHANNEL_LIST_LIMIT, new BaseChannel.GetMessagesHandler() {
//                    @Override
//                    public void onResult(List<BaseMessage> list, SendBirdException e) {
//                        mChatAdapter.markAllMessagesAsRead();
//                    }
//                });
//            }
//        });
//    }

}
