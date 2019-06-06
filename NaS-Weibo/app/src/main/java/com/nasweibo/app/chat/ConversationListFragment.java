package com.nasweibo.app.chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nasweibo.app.R;
import com.nasweibo.app.contact.TimeUnit;
import com.nasweibo.app.data.Conversation;
import com.nasweibo.app.data.Message;
import com.nasweibo.app.data.User;
import com.nasweibo.app.ui.BaseTabFragment;
import com.nasweibo.app.util.AccountUtils;
import com.nasweibo.app.util.Constant;
import com.nasweibo.app.util.DateUtils;
import com.nasweibo.app.util.ImageUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;


public class ConversationListFragment extends BaseTabFragment {

    RecyclerView recyclwConversation;
    ConversationAdapter conversationAdapter;
    private List<Conversation> conversationList = new ArrayList<>();
    private DatabaseReference mDatabase;
    private CircularProgressView progressView;
    private TextView emptyMsg;
    private Timer timer = new Timer();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chat, container, false);
        recyclwConversation = root.findViewById(R.id.rcw_conversation_list);
        emptyMsg = root.findViewById(R.id.empty_conversation_message);
        progressView = root.findViewById(R.id.load_conversation_progress);
        progressView.setVisibility(View.VISIBLE);
        progressView.startAnimation();

        RecyclerView.LayoutManager manager = new LinearLayoutManager(getContext());
        conversationAdapter = new ConversationAdapter(getContext(), conversationList);
        recyclwConversation.setLayoutManager(manager);
        recyclwConversation.setItemAnimator(new DefaultItemAnimator());
        recyclwConversation.setAdapter(conversationAdapter);

        mDatabase.child("conversation").child(AccountUtils.getUID(getContext()))
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.getValue() != null) {
                            Message lastMessage = dataSnapshot.getValue(Message.class);
                            String conversationID = dataSnapshot.getKey();

                            Conversation conversation = new Conversation();
                            conversation.setId(conversationID);
                            conversation.setLastMessage(lastMessage);

                            appendUserInfo(conversation);

                        } else {
                            progressView.stopAnimation();
                            progressView.setVisibility(View.INVISIBLE);
                            emptyMsg.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.getValue() != null) {
                            Message lastMessage = dataSnapshot.getValue(Message.class);
                            String conversationID = dataSnapshot.getKey();

                            Conversation conversation = new Conversation();
                            conversation.setId(conversationID);
                            conversation.setLastMessage(lastMessage);

                            for (int i = 0; i < conversationList.size(); i++) {
                                Conversation oldConversation = conversationList.get(i);
                                if (oldConversation.getId().equals(conversationID)) {
                                    conversation.setFriend(oldConversation.getFriend());
                                    conversationList.set(i, conversation);
                                    conversationAdapter.notifyDataSetChanged();
                                    break;
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
                        progressView.stopAnimation();
                        progressView.setVisibility(View.INVISIBLE);
                        emptyMsg.setVisibility(View.VISIBLE);
                    }
                });

        detectOnlineStatus();

        return root;
    }

    private void detectOnlineStatus() {
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                if(!conversationList.isEmpty()){
                    updateOnlineStatus(0);
                }
            }
        };
        timer.schedule(doAsynchronousTask, 10, 30 * 1000);
    }

    private void updateOnlineStatus(final int index) {
        if (conversationList.isEmpty() || index >= conversationList.size()) {
            conversationAdapter.notifyDataSetChanged();
            Log.d("UPDATE ONLINE", "done");
            return;
        }

        final Conversation conversation = conversationList.get(index);
        final User friend = conversation.getFriend();
        mDatabase.child("users").child(friend.getUid()).child("status").child("onlinestamp")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            long oldTime = (long) dataSnapshot.getValue();
                            friend.setOnline(DateUtils.checkOnlineTimestamp(oldTime));
                            conversation.setFriend(friend);

                            conversationList.set(index, conversation);
                        }

                        updateOnlineStatus(index + 1);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void appendUserInfo(final Conversation conversation) {
        Message message = conversation.getLastMessage();
        String myuid = AccountUtils.getUID(getContext());
        String friendId;
        if (myuid.equals(message.getIdSender())) {
            friendId = message.getIdReceiver();
        } else {
            friendId = message.getIdSender();
        }

        mDatabase.child("users").child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    User friend = dataSnapshot.getValue(User.class);
                    long lastUpdateOnline = friend.getStatus().getOnlinestamp();
                    long currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
                    long delta = currentTime - lastUpdateOnline;
                    long diffMinute = TimeUnit.MILLISECONDS.toMinutes(delta);

                    if (diffMinute <= 1 && delta != 0) {
                        friend.setOnline(true);
                    }
                    conversation.setFriend(friend);
                    conversationList.add(conversation);
                    conversationAdapter.notifyDataSetChanged();

                    progressView.stopAnimation();
                    progressView.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timer.cancel();
    }

    public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

        private Context mContext;
        private List<Conversation> conversationList;

        public ConversationAdapter(Context context, List<Conversation> conversations) {
            this.mContext = context;
            this.conversationList = conversations;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_conversation_layout, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final Conversation conversation = conversationList.get(position);
            final User friend = conversation.getFriend();
            final Message message = conversation.getLastMessage();

            ImageUtils.displayImageFromUrl(mContext, friend.getAvatar(),
                    holder.imgAvatar, mContext.getResources().getDrawable(R.drawable.user_default));

            holder.dateTime.setText(DateUtils.formatTimeWithMarker(message.getTimestamp()));
            if(Message.IMAGE_TYPE.equals(message.getType())){
                holder.tvLastMessage.setText("[Image]");
            }else {
                holder.tvLastMessage.setText(message.getText());
            }

            holder.tvUsername.setText(friend.getName());
            if (friend.isOnline()) {
                holder.userStatus.setVisibility(View.VISIBLE);
            } else {
                holder.userStatus.setVisibility(View.GONE);
            }

            String msgStatus = message.getStatus();
            if (Constant.MSG_NOT_SENT.equals(msgStatus)) {
                holder.msgStatus.setImageResource(R.drawable.not_sent);
            } else if (Constant.MSG_SENT.equals(msgStatus)) {
                holder.msgStatus.setImageResource(R.drawable.sent);
            } else if (Constant.MSG_RECEIVED.equals(msgStatus)) {
                holder.msgStatus.setImageResource(R.drawable.received);
                holder.tvUsername.setTypeface(Typeface.DEFAULT_BOLD);
                holder.tvLastMessage.setTypeface(Typeface.DEFAULT_BOLD);
            } else if (Constant.MSG_READ.equals(msgStatus)) {
                holder.msgStatus.setImageResource(R.drawable.readed);
            } else {
                holder.msgStatus.setVisibility(View.INVISIBLE);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), ChatActivity.class);
                    intent.putExtra("friend", friend);
                    startActivity(intent);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    PopupMenu popup = new PopupMenu(mContext, view);
                    popup.inflate(R.menu.conversation_options);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.delete_conversation:
                                    mDatabase.child("conversation").child(AccountUtils.getUID(mContext))
                                            .child(conversation.getId()).removeValue();
                                    conversationList.remove(conversation);
                                    notifyDataSetChanged();
                                    break;
                            }
                            return false;
                        }
                    });
                    popup.show();

                    return false;
                }
            });

        }

        @Override
        public int getItemCount() {
            return conversationList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            CircleImageView imgAvatar;
            TextView tvUsername, dateTime;
            TextView tvLastMessage;
            ImageView userStatus, msgStatus;

            public ViewHolder(View itemView) {
                super(itemView);
                imgAvatar = itemView.findViewById(R.id.imv_avatar);
                tvUsername = itemView.findViewById(R.id.tv_username);
                tvLastMessage = itemView.findViewById(R.id.tv_message);
                userStatus = itemView.findViewById(R.id.user_status);
                msgStatus = itemView.findViewById(R.id.msg_status);
                dateTime = itemView.findViewById(R.id.tv_time);
            }
        }
    }

}
