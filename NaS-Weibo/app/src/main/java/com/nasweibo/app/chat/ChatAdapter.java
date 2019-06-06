package com.nasweibo.app.chat;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nasweibo.app.R;
import com.nasweibo.app.data.Message;
import com.nasweibo.app.util.AccountUtils;
import com.nasweibo.app.util.DateUtils;
import com.nasweibo.app.util.ImageUtils;
import com.nasweibo.app.util.UrlPreviewInfo;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String URL_PREVIEW_CUSTOM_TYPE = "link";

    private static final int VIEW_TYPE_USER_MESSAGE_ME = 10;
    private static final int VIEW_TYPE_USER_MESSAGE_OTHER = 11;
    private static final int VIEW_TYPE_FILE_MESSAGE_ME = 20;
    private static final int VIEW_TYPE_FILE_MESSAGE_OTHER = 21;
    private static final int VIEW_TYPE_FILE_MESSAGE_IMAGE_ME = 22;
    private static final int VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER = 23;
    private static final int VIEW_TYPE_FILE_MESSAGE_VIDEO_ME = 24;
    private static final int VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER = 25;
    private static final int VIEW_TYPE_ADMIN_MESSAGE = 30;

    private Context mContext;
//    private HashMap<FileMessage, CircleProgressBar> mFileMessageMap;

    //    private GroupChannel mChannel;
    private List<Message> mMessageList;

    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;

    private ArrayList<String> mFailedMessageIdList = new ArrayList<>();
    private Hashtable<String, Uri> mTempFileMessageUriTable = new Hashtable<>();
    private boolean mIsMessageListLoading;

    interface OnItemLongClickListener {
        void onUserMessageItemLongClick(Message message, int position);

//        void onFileMessageItemLongClick(FileMessage message);
//
//        void onAdminMessageItemLongClick(AdminMessage message);
    }

    interface OnItemClickListener {
        void onUserMessageItemClick(Message message);

        void onFileMessageItemClick(Message message);
    }


    ChatAdapter(Context context, List<Message> messageList) {
        mContext = context;
//        mFileMessageMap = new HashMap<>();
        mMessageList = messageList;
    }

    void setContext(Context context) {
        mContext = context;
    }

    public void load(String channelUrl) {
        try {
//            File appDir = new File(mContext.getCacheDir(), SendBird.getApplicationId());
//            appDir.mkdirs();
//
//            File dataFile = new File(appDir, TextUtils.generateMD5(SendBird.getCurrentUser().getUserId() + channelUrl) + ".data");
//
//            String content = FileUtils.loadFromFile(dataFile);
//            String [] dataArray = content.split("\n");
//
//            mChannel = (GroupChannel) GroupChannel.buildFromSerializedData(Base64.decode(dataArray[0], Base64.DEFAULT | Base64.NO_WRAP));

            // Reset message list, then add cached messages.
            mMessageList.clear();
//            for(int i = 1; i < dataArray.length; i++) {
            for (int i = 1; i < 10; i++) {
//                mMessageList.add(BaseMessage.buildFromSerializedData(Base64.decode(dataArray[i], Base64.DEFAULT | Base64.NO_WRAP)));
                Message message = new Message("this is message " + i);
                message.setStatus(Message.SENT_STATUS);
                if (i % 2 == 0) {
                    message.setIdSender(AccountUtils.getUID(mContext));
                } else {
                    message.setIdSender("other");
                }
                mMessageList.add(message);
            }

            notifyDataSetChanged();
        } catch (Exception e) {
            // Nothing to load.
        }
    }

//    public void save() {
//        try {
//            StringBuilder sb = new StringBuilder();
//            if (mChannel != null) {
//                // Convert current data into string.
//                sb.append(Base64.encodeToString(mChannel.serialize(), Base64.DEFAULT | Base64.NO_WRAP));
//                BaseMessage message = null;
//                for (int i = 0; i < Math.min(mMessageList.size(), 100); i++) {
//                    message = mMessageList.get(i);
//                    if (!isTempMessage(message)) {
//                        sb.append("\n");
//                        sb.append(Base64.encodeToString(message.serialize(), Base64.DEFAULT | Base64.NO_WRAP));
//                    }
//                }
//
//                String data = sb.toString();
//                String md5 = TextUtils.generateMD5(data);
//
//                // Save the data into file.
//                File appDir = new File(mContext.getCacheDir(), SendBird.getApplicationId());
//                appDir.mkdirs();
//
//                File hashFile = new File(appDir, TextUtils.generateMD5(SendBird.getCurrentUser().getUserId() + mChannel.getUrl()) + ".hash");
//                File dataFile = new File(appDir, TextUtils.generateMD5(SendBird.getCurrentUser().getUserId() + mChannel.getUrl()) + ".data");
//
//                try {
//                    String content = FileUtils.loadFromFile(hashFile);
//                    // If data has not been changed, do not save.
//                    if(md5.equals(content)) {
//                        return;
//                    }
//                } catch(IOException e) {
//                    // File not found. Save the data.
//                }
//
//                FileUtils.saveToFile(dataFile, data);
//                FileUtils.saveToFile(hashFile, md5);
//            }
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * Inflates the correct layout according to the View Type.
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case VIEW_TYPE_USER_MESSAGE_ME:
                View myUserMsgView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_group_chat_user_me, parent, false);
                return new MyUserMessageHolder(myUserMsgView);
            case VIEW_TYPE_USER_MESSAGE_OTHER:
                View otherUserMsgView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_group_chat_user_other, parent, false);
                return new OtherUserMessageHolder(otherUserMsgView);
            case VIEW_TYPE_ADMIN_MESSAGE:
                View adminMsgView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_group_chat_admin, parent, false);
                return new AdminMessageHolder(adminMsgView);
            case VIEW_TYPE_FILE_MESSAGE_ME:
                View myFileMsgView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_group_chat_file_me, parent, false);
                return new MyFileMessageHolder(myFileMsgView);
            case VIEW_TYPE_FILE_MESSAGE_OTHER:
                View otherFileMsgView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_group_chat_file_other, parent, false);
                return new OtherFileMessageHolder(otherFileMsgView);
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_ME:
                View myImageFileMsgView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_group_chat_file_image_me, parent, false);
                return new MyImageFileMessageHolder(myImageFileMsgView);
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER:
                View otherImageFileMsgView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_group_chat_file_image_other, parent, false);
                return new OtherImageFileMessageHolder(otherImageFileMsgView);
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_ME:
                View myVideoFileMsgView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_group_chat_file_video_me, parent, false);
                return new MyVideoFileMessageHolder(myVideoFileMsgView);
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER:
                View otherVideoFileMsgView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_group_chat_file_video_other, parent, false);
                return new OtherVideoFileMessageHolder(otherVideoFileMsgView);

            default:
                return null;

        }
    }

    /**
     * Binds variables in the BaseMessage to UI components in the ViewHolder.
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = mMessageList.get(position);
        boolean isContinuous = false;
        boolean isNewDay = false;
        boolean isTempMessage = false;
        boolean isFailedMessage = false;
        Uri tempFileMessageUri = null;

        // If there is at least one item preceding the current one, check the previous message.
        if (position < mMessageList.size() - 1) {
            Message prevMessage = mMessageList.get(position + 1);

            // If the date of the previous message is different, display the date before the message,
            // and also set isContinuous to false to show information such as the sender's nickname
            // and profile image.
            if (!DateUtils.hasSameDate(message.getTimestamp(), prevMessage.getTimestamp())) {
                isNewDay = true;
                isContinuous = false;
            } else {
                isContinuous = isContinuous(message, prevMessage);
            }
        } else if (position == mMessageList.size() - 1) {
            isNewDay = true;
        }

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_USER_MESSAGE_ME:
                ((MyUserMessageHolder) holder).bind(mContext, message, isContinuous, isNewDay, mItemClickListener, mItemLongClickListener, position);
                break;
            case VIEW_TYPE_USER_MESSAGE_OTHER:
                ((OtherUserMessageHolder) holder).bind(mContext, message, isNewDay, isContinuous, mItemClickListener, mItemLongClickListener, position);
                break;
//            case VIEW_TYPE_ADMIN_MESSAGE:
//                ((AdminMessageHolder) holder).bind(mContext, (AdminMessage) message, mChannel, isNewDay);
//                break;
//            case VIEW_TYPE_FILE_MESSAGE_ME:
//                ((MyFileMessageHolder) holder).bind(mContext, (FileMessage) message, mChannel, isNewDay, isTempMessage, isFailedMessage, tempFileMessageUri, mItemClickListener);
//                break;
//            case VIEW_TYPE_FILE_MESSAGE_OTHER:
//                ((OtherFileMessageHolder) holder).bind(mContext, (FileMessage) message, mChannel, isNewDay, isContinuous, mItemClickListener);
//                break;
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_ME:
                ((MyImageFileMessageHolder) holder).bind(mContext, message, isNewDay, mItemClickListener);
                break;
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER:
                ((OtherImageFileMessageHolder) holder).bind(mContext, message, isNewDay, isContinuous, mItemClickListener);
                break;
//            case VIEW_TYPE_FILE_MESSAGE_VIDEO_ME:
//                ((MyVideoFileMessageHolder) holder).bind(mContext, (FileMessage) message, mChannel, isNewDay, isTempMessage, isFailedMessage, tempFileMessageUri, mItemClickListener);
//                break;
//            case VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER:
//                ((OtherVideoFileMessageHolder) holder).bind(mContext, (FileMessage) message, mChannel, isNewDay, isContinuous, mItemClickListener);
//                break;
            default:
                break;
        }
    }

    /**
     * Declares the View Type according to the type of message and the sender.
     */
    @Override
    public int getItemViewType(int position) {

        Message message = mMessageList.get(position);

        if (Message.TEXT_TYPE.equals(message.getType()) || Message.URL_TYPE.equals(message.getType())) {
            if (AccountUtils.getUID(mContext).equals(message.getIdSender())) {
                return VIEW_TYPE_USER_MESSAGE_ME;
            } else {
                return VIEW_TYPE_USER_MESSAGE_OTHER;
            }
        } else {
            if (AccountUtils.getUID(mContext).equals(message.getIdSender())) {
                return VIEW_TYPE_FILE_MESSAGE_IMAGE_ME;
            } else {
                return VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER;
            }
        }
        // If the sender is current user

//        } else if (message instanceof FileMessage) {
//            FileMessage fileMessage = (FileMessage) message;
//            if (fileMessage.getType().toLowerCase().startsWith("image")) {
//                // If the sender is current user
//                if (fileMessage.getSender().getUserId().equals(SendBird.getCurrentUser().getUserId())) {
//                    return VIEW_TYPE_FILE_MESSAGE_IMAGE_ME;
//                } else {
//                    return VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER;
//                }
//            } else if (fileMessage.getType().toLowerCase().startsWith("video")) {
//                if (fileMessage.getSender().getUserId().equals(SendBird.getCurrentUser().getUserId())) {
//                    return VIEW_TYPE_FILE_MESSAGE_VIDEO_ME;
//                } else {
//                    return VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER;
//                }
//            } else {
//                if (fileMessage.getSender().getUserId().equals(SendBird.getCurrentUser().getUserId())) {
//                    return VIEW_TYPE_FILE_MESSAGE_ME;
//                } else {
//                    return VIEW_TYPE_FILE_MESSAGE_OTHER;
//                }
//            }
//        } else if (message instanceof AdminMessage) {
//            return VIEW_TYPE_ADMIN_MESSAGE;
//        }

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

//    void setChannel(GroupChannel channel) {
//        mChannel = channel;
//    }

//    public boolean isTempMessage(BaseMessage message) {
//        return message.getMessageId() == 0;
//    }

//    public boolean isFailedMessage(BaseMessage message) {
//        if (!isTempMessage(message)) {
//            return false;
//        }
//
//        if (message instanceof UserMessage) {
//            int index = mFailedMessageIdList.indexOf(((UserMessage) message).getRequestId());
//            if (index >= 0) {
//                return true;
//            }
//        } else if (message instanceof FileMessage) {
//            int index = mFailedMessageIdList.indexOf(((FileMessage) message).getRequestId());
//            if (index >= 0) {
//                return true;
//            }
//        }
//
//        return false;
//    }


//    public Uri getTempFileMessageUri(BaseMessage message) {
//        if (!isTempMessage(message)) {
//            return null;
//        }
//
//        if (!(message instanceof FileMessage)) {
//            return null;
//        }
//
//        return mTempFileMessageUriTable.get(((FileMessage) message).getRequestId());
//    }

    //
    public void markMessageFailed(String requestId) {
        mFailedMessageIdList.add(requestId);
        notifyDataSetChanged();
    }

    //
//    public void removeFailedMessage(BaseMessage message) {
//        if (message instanceof UserMessage) {
//            mFailedMessageIdList.remove(((UserMessage) message).getRequestId());
//            mMessageList.remove(message);
//        } else if (message instanceof FileMessage) {
//            mFailedMessageIdList.remove(((FileMessage) message).getRequestId());
//            mTempFileMessageUriTable.remove(((FileMessage) message).getRequestId());
//            mMessageList.remove(message);
//        }
//
//        notifyDataSetChanged();
//    }
//
    void startProgressUploadFile() {
        //TODO start animation progressbar to upload file
    }

//    public void markMessageSent(BaseMessage message) {
//        BaseMessage msg;
//        for (int i = mMessageList.size() - 1; i >= 0; i--) {
//            msg = mMessageList.get(i);
//            if (message instanceof UserMessage && msg instanceof UserMessage) {
//                if (((UserMessage) msg).getRequestId().equals(((UserMessage) message).getRequestId())) {
//                    mMessageList.set(i, message);
//                    notifyDataSetChanged();
//                    return;
//                }
//            } else if (message instanceof FileMessage && msg instanceof FileMessage) {
//                if (((FileMessage) msg).getRequestId().equals(((FileMessage) message).getRequestId())) {
//                    mTempFileMessageUriTable.remove(((FileMessage) message).getRequestId());
//                    mMessageList.set(i, message);
//                    notifyDataSetChanged();
//                    return;
//                }
//            }
//        }
//    }
//
//    void addTempFileMessageInfo(FileMessage message, Uri uri) {
//        mTempFileMessageUriTable.put(message.getRequestId(), uri);
//    }

    void addFirst(Message message) {
        mMessageList.add(0, message);
        notifyDataSetChanged();
    }


    void deleteTempMessage(long timestamp) {
        for(Message msg : mMessageList) {
            if(msg.getTimestamp() == timestamp) {
                mMessageList.remove(msg);
                break;
            }
        }
    }

//    void update(BaseMessage message) {
//        BaseMessage baseMessage;
//        for (int index = 0; index < mMessageList.size(); index++) {
//            baseMessage = mMessageList.get(index);
//            if(message.getMessageId() == baseMessage.getMessageId()) {
//                mMessageList.remove(index);
//                mMessageList.add(index, message);
//                notifyDataSetChanged();
//                break;
//            }
//        }
//    }

    private synchronized boolean isMessageListLoading() {
        return mIsMessageListLoading;
    }

    private synchronized void setMessageListLoading(boolean tf) {
        mIsMessageListLoading = tf;
    }

    /**
     * Notifies that the user has read all (previously unread) messages in the channel.
     * Typically, this would be called immediately after the user enters the chat and loads
     * its most recent messages.
     */
//    public void markAllMessagesAsRead() {
//        if (mChannel != null) {
//            mChannel.markAsRead();
//        }
//        notifyDataSetChanged();
//    }

    /**
     * Load old message list.
     * @param limit
     * @param handler
     */
//    public void loadPreviousMessages(int limit, final BaseChannel.GetMessagesHandler handler) {
//        if(isMessageListLoading()) {
//            return;
//        }
//
//        long oldestMessageCreatedAt = Long.MAX_VALUE;
//        if(mMessageList.size() > 0) {
//            oldestMessageCreatedAt = mMessageList.get(mMessageList.size() - 1).getCreatedAt();
//        }
//
//        setMessageListLoading(true);
//        mChannel.getPreviousMessagesByTimestamp(oldestMessageCreatedAt, false, limit, true, BaseChannel.MessageTypeFilter.ALL, null, new BaseChannel.GetMessagesHandler() {
//            @Override
//            public void onResult(List<BaseMessage> list, SendBirdException e) {
//                if(handler != null) {
//                    handler.onResult(list, e);
//                }
//
//                setMessageListLoading(false);
//                if(e != null) {
//                    e.printStackTrace();
//                    return;
//                }
//
//                for(BaseMessage message : list) {
//                    mMessageList.add(message);
//                }
//
//                notifyDataSetChanged();
//            }
//        });
//    }

    /**
     * Replaces current message list with new list.
     * Should be used only on initial load or refresh.
     */
//    public void loadLatestMessages(int limit, final BaseChannel.GetMessagesHandler handler) {
//        if(isMessageListLoading()) {
//            return;
//        }
//
//        setMessageListLoading(true);
//        mChannel.getPreviousMessagesByTimestamp(Long.MAX_VALUE, true, limit, true, BaseChannel.MessageTypeFilter.ALL, null, new BaseChannel.GetMessagesHandler() {
//            @Override
//            public void onResult(List<BaseMessage> list, SendBirdException e) {
//                if(handler != null) {
//                    handler.onResult(list, e);
//                }
//
//                setMessageListLoading(false);
//                if(e != null) {
//                    e.printStackTrace();
//                    return;
//                }
//
//                if(list.size() <= 0) {
//                    return;
//                }
//
//                for (BaseMessage message : mMessageList) {
//                    if (isTempMessage(message) || isFailedMessage(message)) {
//                        list.add(0, message);
//                    }
//                }
//
//                mMessageList.clear();
//
//                for(BaseMessage message : list) {
//                    mMessageList.add(message);
//                }
//
//                notifyDataSetChanged();
//            }
//        });
//    }
    public void setItemLongClickListener(OnItemLongClickListener listener) {
        mItemLongClickListener = listener;
    }

    public void setItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    /**
     * Checks if the current message was sent by the same person that sent the preceding message.
     * <p>
     * This is done so that redundant UI, such as sender nickname and profile picture,
     * does not have to displayed when not necessary.
     */
    private boolean isContinuous(Message currentMsg, Message precedingMsg) {
        // null check
        if (currentMsg == null || precedingMsg == null) {
            return false;
        }

        if (currentMsg.getIdSender().equals(precedingMsg.getIdSender())) {
            return true;
        }

        return false;
    }


    private class AdminMessageHolder extends RecyclerView.ViewHolder {
        private TextView messageText, dateText;

        AdminMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_group_chat_message);
            dateText = (TextView) itemView.findViewById(R.id.text_group_chat_date);
        }

//        void bind(Context context, AdminMessage message, GroupChannel channel, boolean isNewDay) {
//            messageText.setText(message.getMessage());
//
//            if (isNewDay) {
//                dateText.setVisibility(View.VISIBLE);
//                dateText.setText(DateUtils.formatDate(message.getCreatedAt()));
//            } else {
//                dateText.setVisibility(View.GONE);
//            }
//        }
    }

    private class MyUserMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, editedText, timeText, dateText;
        ViewGroup urlPreviewContainer;
        TextView urlPreviewSiteNameText, urlPreviewTitleText, urlPreviewDescriptionText;
        ImageView urlPreviewMainImageView, imgStatusMsg;
        View padding;

        MyUserMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_group_chat_message);
            editedText = (TextView) itemView.findViewById(R.id.text_group_chat_edited);
            timeText = (TextView) itemView.findViewById(R.id.text_group_chat_time);
            imgStatusMsg = itemView.findViewById(R.id.status_msg);
            dateText = (TextView) itemView.findViewById(R.id.text_group_chat_date);

            urlPreviewContainer = (ViewGroup) itemView.findViewById(R.id.url_preview_container);
            urlPreviewSiteNameText = (TextView) itemView.findViewById(R.id.text_url_preview_site_name);
            urlPreviewTitleText = (TextView) itemView.findViewById(R.id.text_url_preview_title);
            urlPreviewDescriptionText = (TextView) itemView.findViewById(R.id.text_url_preview_description);
            urlPreviewMainImageView = (ImageView) itemView.findViewById(R.id.image_url_preview_main);

            // Dynamic padding that can be hidden or shown based on whether the message is continuous.
            padding = itemView.findViewById(R.id.view_group_chat_padding);
        }

        void bind(Context context, final Message message, boolean isContinuous, boolean isNewDay, final OnItemClickListener clickListener, final OnItemLongClickListener longClickListener, final int position) {
            // If continuous from previous message, remove extra padding.
            if (isContinuous) {
                padding.setVisibility(View.GONE);
            } else {
                padding.setVisibility(View.VISIBLE);
            }

            // If the message is sent on a different date than the previous one, display the date.
            if (isNewDay) {
                dateText.setVisibility(View.VISIBLE);
                dateText.setText(DateUtils.formatDate(message.getTimestamp()));
            } else {
                dateText.setVisibility(View.GONE);
            }

            messageText.setText(message.getText());
            timeText.setText(DateUtils.formatTime(message.getTimestamp()));

//            if (message.get() > 0) {
//                editedText.setVisibility(View.VISIBLE);
//            } else {
//                editedText.setVisibility(View.GONE);
//            }

            imgStatusMsg.setVisibility(View.VISIBLE);
            switch (message.getStatus()) {
                case Message.SENDING_STATUS:
                    imgStatusMsg.setImageResource(R.drawable.not_sent);
                    break;
                case Message.SENT_STATUS:
                    imgStatusMsg.setImageResource(R.drawable.sent);
                    break;
                case Message.RECEIVED_STATUS:
                    imgStatusMsg.setImageResource(R.drawable.received);
                    break;
                case Message.SEEN_STATUS:
                    imgStatusMsg.setImageResource(R.drawable.readed);
                    break;
                default:
                    imgStatusMsg.setVisibility(View.INVISIBLE);
            }

            urlPreviewContainer.setVisibility(View.GONE);
            if (message.getType().equals(URL_PREVIEW_CUSTOM_TYPE)) {
                try {
                    urlPreviewContainer.setVisibility(View.VISIBLE);
                    final UrlPreviewInfo previewInfo = new UrlPreviewInfo(message.getValue());
                    urlPreviewSiteNameText.setText("@" + previewInfo.getSiteName());
                    urlPreviewTitleText.setText(previewInfo.getTitle());
                    urlPreviewDescriptionText.setText(previewInfo.getDescription());
                    ImageUtils.displayImageFromUrl(context, previewInfo.getImageUrl(), urlPreviewMainImageView, null);
                } catch (JSONException e) {
                    urlPreviewContainer.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }

            if (clickListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickListener.onUserMessageItemClick(message);
                    }
                });
            }

            if (longClickListener != null) {
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
//                        longClickListener.onUserMessageItemLongClick(message, position);
                        return true;
                    }
                });
            }
        }

    }

    private class OtherUserMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, editedText, timeText, readReceiptText, dateText;
        ImageView profileImage;

        ViewGroup urlPreviewContainer;
        TextView urlPreviewSiteNameText, urlPreviewTitleText, urlPreviewDescriptionText;
        ImageView urlPreviewMainImageView;

        public OtherUserMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_group_chat_message);
            editedText = (TextView) itemView.findViewById(R.id.text_group_chat_edited);
            timeText = (TextView) itemView.findViewById(R.id.text_group_chat_time);
            profileImage = (ImageView) itemView.findViewById(R.id.image_group_chat_profile);
            readReceiptText = (TextView) itemView.findViewById(R.id.text_group_chat_read_receipt);
            dateText = (TextView) itemView.findViewById(R.id.text_group_chat_date);

            urlPreviewContainer = (ViewGroup) itemView.findViewById(R.id.url_preview_container);
            urlPreviewSiteNameText = (TextView) itemView.findViewById(R.id.text_url_preview_site_name);
            urlPreviewTitleText = (TextView) itemView.findViewById(R.id.text_url_preview_title);
            urlPreviewDescriptionText = (TextView) itemView.findViewById(R.id.text_url_preview_description);
            urlPreviewMainImageView = (ImageView) itemView.findViewById(R.id.image_url_preview_main);
        }


        void bind(final Context context, final Message message, boolean isNewDay, boolean isContinuous, final OnItemClickListener clickListener, final OnItemLongClickListener longClickListener, final int position) {

            // Since setChannel is set slightly after adapter is created
//            if (channel != null) {
//                int readReceipt = channel.getReadReceipt(message);
//                if (readReceipt > 0) {
//                    readReceiptText.setVisibility(View.VISIBLE);
//                    readReceiptText.setText(String.valueOf(readReceipt));
//                } else {
//                    readReceiptText.setVisibility(View.INVISIBLE);
//                }
//            }

            // Show the date if the message was sent on a different date than the previous message.
            if (isNewDay) {
                dateText.setVisibility(View.VISIBLE);
                dateText.setText(DateUtils.formatDate(message.getTimestamp()));
            } else {
                dateText.setVisibility(View.GONE);
            }

            // Hide profile image and nickname if the previous message was also sent by current sender.
            if (isContinuous) {
                profileImage.setVisibility(View.INVISIBLE);
            } else {
                profileImage.setVisibility(View.VISIBLE);
                FirebaseDatabase.getInstance().getReference().child("users")
                        .child(message.getIdSender()).child("avatar")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) {
                                    String url = (String) dataSnapshot.getValue();
                                    if (context != null) {
                                        ImageUtils.displayRoundImageFromUrl(context, url, profileImage);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

            messageText.setText(message.getText());
            timeText.setText(DateUtils.formatTime(message.getTimestamp()));

//            if (message.getUpdatedAt() > 0) {
//                editedText.setVisibility(View.VISIBLE);
//            } else {
//                editedText.setVisibility(View.GONE);
//            }

            urlPreviewContainer.setVisibility(View.GONE);
            if (message.getType().equals(URL_PREVIEW_CUSTOM_TYPE)) {
                try {
                    urlPreviewContainer.setVisibility(View.VISIBLE);
                    UrlPreviewInfo previewInfo = new UrlPreviewInfo(message.getValue());
                    urlPreviewSiteNameText.setText("@" + previewInfo.getSiteName());
                    urlPreviewTitleText.setText(previewInfo.getTitle());
                    urlPreviewDescriptionText.setText(previewInfo.getDescription());
                    ImageUtils.displayImageFromUrl(context, previewInfo.getImageUrl(), urlPreviewMainImageView, null);
                } catch (JSONException e) {
                    urlPreviewContainer.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }


            if (clickListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickListener.onUserMessageItemClick(message);
                    }
                });
            }
            if (longClickListener != null) {
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        longClickListener.onUserMessageItemLongClick(message, position);
                        return true;
                    }
                });
            }
        }
    }

    private class MyFileMessageHolder extends RecyclerView.ViewHolder {
        TextView fileNameText, timeText, readReceiptText, dateText;
//        CircularProgressView circleProgressBar;

        public MyFileMessageHolder(View itemView) {
            super(itemView);

            timeText = (TextView) itemView.findViewById(R.id.text_group_chat_time);
            fileNameText = (TextView) itemView.findViewById(R.id.text_group_chat_file_name);
            readReceiptText = (TextView) itemView.findViewById(R.id.text_group_chat_read_receipt);
//            circleProgressBar = (CircularProgressView) itemView.findViewById(R.id.upload_img_progress);
//            dateText = (TextView) itemView.findViewById(R.id.text_group_chat_date);
        }

//        void bind(Context context, final FileMessage message, GroupChannel channel, boolean isNewDay, boolean isTempMessage, boolean isFailedMessage, Uri tempFileMessageUri, final OnItemClickListener listener) {
//            fileNameText.setText(message.getName());
//            timeText.setText(DateUtils.formatTime(message.getCreatedAt()));
//
//            if (isFailedMessage) {
//                readReceiptText.setText(R.string.message_failed);
//                readReceiptText.setVisibility(View.VISIBLE);
//
//                circleProgressBar.setVisibility(View.GONE);
//                mFileMessageMap.remove(message);
//            } else if (isTempMessage) {
//                readReceiptText.setText(R.string.message_sending);
//                readReceiptText.setVisibility(View.GONE);
//
//                circleProgressBar.setVisibility(View.VISIBLE);
//                mFileMessageMap.put(message, circleProgressBar);
//            } else {
//                circleProgressBar.setVisibility(View.GONE);
//                mFileMessageMap.remove(message);
//
//                if (channel != null) {
//                    int readReceipt = channel.getReadReceipt(message);
//                    if (readReceipt > 0) {
//                        readReceiptText.setVisibility(View.VISIBLE);
//                        readReceiptText.setText(String.valueOf(readReceipt));
//                    } else {
//                        readReceiptText.setVisibility(View.INVISIBLE);
//                    }
//                }
//
//            }
//            // Show the date if the message was sent on a different date than the previous message.
//            if (isNewDay) {
//                dateText.setVisibility(View.VISIBLE);
//                dateText.setText(DateUtils.formatDate(message.getCreatedAt()));
//            } else {
//                dateText.setVisibility(View.GONE);
//            }
//
//            if (listener != null) {
//                itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        listener.onFileMessageItemClick(message);
//                    }
//                });
//            }
//        }
    }

    private class OtherFileMessageHolder extends RecyclerView.ViewHolder {
        TextView nicknameText, timeText, fileNameText, fileSizeText, dateText, readReceiptText;
        ImageView profileImage;

        public OtherFileMessageHolder(View itemView) {
            super(itemView);

            nicknameText = (TextView) itemView.findViewById(R.id.text_group_chat_nickname);
            timeText = (TextView) itemView.findViewById(R.id.text_group_chat_time);
            fileNameText = (TextView) itemView.findViewById(R.id.text_group_chat_file_name);
//            fileSizeText = (TextView) itemView.findViewById(R.id.text_group_chat_file_size);

            profileImage = (ImageView) itemView.findViewById(R.id.image_group_chat_profile);
            dateText = (TextView) itemView.findViewById(R.id.text_group_chat_date);
            readReceiptText = (TextView) itemView.findViewById(R.id.text_group_chat_read_receipt);
        }

//        void bind(Context context, final FileMessage message, GroupChannel channel, boolean isNewDay, boolean isContinuous, final OnItemClickListener listener) {
//            fileNameText.setText(message.getName());
//            timeText.setText(DateUtils.formatTime(message.getCreatedAt()));
////            fileSizeText.setText(String.valueOf(message.getSize()));
//
//            // Since setChannel is set slightly after adapter is created, check if null.
//            if (channel != null) {
//                int readReceipt = channel.getReadReceipt(message);
//                if (readReceipt > 0) {
//                    readReceiptText.setVisibility(View.VISIBLE);
//                    readReceiptText.setText(String.valueOf(readReceipt));
//                } else {
//                    readReceiptText.setVisibility(View.INVISIBLE);
//                }
//            }
//
//            // Show the date if the message was sent on a different date than the previous message.
//            if (isNewDay) {
//                dateText.setVisibility(View.VISIBLE);
//                dateText.setText(DateUtils.formatDate(message.getCreatedAt()));
//            } else {
//                dateText.setVisibility(View.GONE);
//            }
//
//            // Hide profile image and nickname if the previous message was also sent by current sender.
//            if (isContinuous) {
//                profileImage.setVisibility(View.INVISIBLE);
//                nicknameText.setVisibility(View.GONE);
//            } else {
//                profileImage.setVisibility(View.VISIBLE);
//                ImageUtils.displayRoundImageFromUrl(context, message.getSender().getProfileUrl(), profileImage);
//
//                nicknameText.setVisibility(View.VISIBLE);
//                nicknameText.setText(message.getSender().getNickname());
//            }
//
//            if (listener != null) {
//                itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        listener.onFileMessageItemClick(message);
//                    }
//                });
//            }
//        }
    }

    /**
     * A ViewHolder for file messages that are images.
     * Displays only the image thumbnail.
     */
    private class MyImageFileMessageHolder extends RecyclerView.ViewHolder {
        TextView timeText, dateText;
        ImageView fileThumbnailImage;
//        CircularProgressView circleProgressBar;

        public MyImageFileMessageHolder(View itemView) {
            super(itemView);

            timeText = itemView.findViewById(R.id.text_group_chat_time);
            fileThumbnailImage = itemView.findViewById(R.id.image_group_chat_file_thumbnail);
//            circleProgressBar = itemView.findViewById(R.id.upload_img_progress);
            dateText = itemView.findViewById(R.id.text_group_chat_date);
        }

        void bind(Context context, final Message message, boolean isNewDay, final OnItemClickListener listener) {
            timeText.setText(DateUtils.formatTime(message.getTimestamp()));

            // Show the date if the message was sent on a different date than the previous message.
            if (isNewDay) {
                dateText.setVisibility(View.VISIBLE);
                dateText.setText(DateUtils.formatDate(message.getTimestamp()));
            } else {
                dateText.setVisibility(View.GONE);
            }

            if (message.isTempMessage()) {
                ImageUtils.displayImageFromUrl(context, "", fileThumbnailImage, null);
            } else {
                if (message.getType().toLowerCase().contains("gif")) {
                    ImageUtils.displayGifImageFromUrl(context, message.getValue(), fileThumbnailImage, (String) null, fileThumbnailImage.getDrawable());
                } else {
                    ImageUtils.displayImageFromUrl(context, message.getValue(), fileThumbnailImage,null);
                }
            }

            if (listener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onFileMessageItemClick(message);
                    }
                });
            }
        }
    }

    private class OtherImageFileMessageHolder extends RecyclerView.ViewHolder {

        TextView timeText, dateText;
        ImageView profileImage, fileThumbnailImage;

        public OtherImageFileMessageHolder(View itemView) {
            super(itemView);

            timeText = (TextView) itemView.findViewById(R.id.text_group_chat_time);
            fileThumbnailImage = (ImageView) itemView.findViewById(R.id.image_group_chat_file_thumbnail);
            profileImage = (ImageView) itemView.findViewById(R.id.image_group_chat_profile);
            dateText = (TextView) itemView.findViewById(R.id.text_group_chat_date);
        }

        void bind(Context context, final Message message, boolean isNewDay, boolean isContinuous, final OnItemClickListener listener) {
            timeText.setText(DateUtils.formatTime(message.getTimestamp()));

            // Show the date if the message was sent on a different date than the previous message.
            if (isNewDay) {
                dateText.setVisibility(View.VISIBLE);
                dateText.setText(DateUtils.formatDate(message.getTimestamp()));
            } else {
                dateText.setVisibility(View.GONE);
            }

            // Hide profile image and nickname if the previous message was also sent by current sender.
            if (isContinuous) {
                profileImage.setVisibility(View.INVISIBLE);
            } else {
                profileImage.setVisibility(View.VISIBLE);
                FirebaseDatabase.getInstance().getReference().child("users")
                        .child(message.getIdSender()).child("avatar")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) {
                                    String url = (String) dataSnapshot.getValue();
                                    if (mContext != null) {
                                        ImageUtils.displayRoundImageFromUrl(mContext, url, profileImage);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

            if (message.getType().toLowerCase().contains("gif")) {
                ImageUtils.displayGifImageFromUrl(context, message.getValue(), fileThumbnailImage, (String) null, fileThumbnailImage.getDrawable());
            } else {
                ImageUtils.displayImageFromUrl(context, message.getValue(), fileThumbnailImage, fileThumbnailImage.getDrawable());
            }

            if (listener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onFileMessageItemClick(message);
                    }
                });
            }
        }
    }

    /**
     * A ViewHolder for file messages that are videos.
     * Displays only the video thumbnail.
     */
    private class MyVideoFileMessageHolder extends RecyclerView.ViewHolder {
        TextView timeText, readReceiptText, dateText;
        ImageView fileThumbnailImage;
//        CircleProgressBar circleProgressBar;

        public MyVideoFileMessageHolder(View itemView) {
            super(itemView);

            timeText = (TextView) itemView.findViewById(R.id.text_group_chat_time);
            fileThumbnailImage = (ImageView) itemView.findViewById(R.id.image_group_chat_file_thumbnail);
            readReceiptText = (TextView) itemView.findViewById(R.id.text_group_chat_read_receipt);
//            circleProgressBar = (CircleProgressBar) itemView.findViewById(R.id.circle_progress);
            dateText = (TextView) itemView.findViewById(R.id.text_group_chat_date);
        }

//        void bind(Context context, final FileMessage message, GroupChannel channel, boolean isNewDay, boolean isTempMessage, boolean isFailedMessage, Uri tempFileMessageUri, final OnItemClickListener listener) {
//            timeText.setText(DateUtils.formatTime(message.getCreatedAt()));
//
//            if (isFailedMessage) {
//                readReceiptText.setText(R.string.message_failed);
//                readReceiptText.setVisibility(View.VISIBLE);
//
//                circleProgressBar.setVisibility(View.GONE);
//                mFileMessageMap.remove(message);
//            } else if (isTempMessage) {
//                readReceiptText.setText(R.string.message_sending);
//                readReceiptText.setVisibility(View.GONE);
//
//                circleProgressBar.setVisibility(View.VISIBLE);
//                mFileMessageMap.put(message, circleProgressBar);
//            } else {
//                circleProgressBar.setVisibility(View.GONE);
//                mFileMessageMap.remove(message);
//
//                // Since setChannel is set slightly after adapter is created, check if null.
//                if (channel != null) {
//                    int readReceipt = channel.getReadReceipt(message);
//                    if (readReceipt > 0) {
//                        readReceiptText.setVisibility(View.VISIBLE);
//                        readReceiptText.setText(String.valueOf(readReceipt));
//                    } else {
//                        readReceiptText.setVisibility(View.INVISIBLE);
//                    }
//                }
//            }
//
//            // Show the date if the message was sent on a different date than the previous message.
//            if (isNewDay) {
//                dateText.setVisibility(View.VISIBLE);
//                dateText.setText(DateUtils.formatDate(message.getCreatedAt()));
//            } else {
//                dateText.setVisibility(View.GONE);
//            }
//
//            if (isTempMessage && tempFileMessageUri != null) {
//                ImageUtils.displayImageFromUrl(context, tempFileMessageUri.toString(), fileThumbnailImage, null);
//            } else {
//                // Get thumbnails from FileMessage
//                ArrayList<FileMessage.Thumbnail> thumbnails = (ArrayList<FileMessage.Thumbnail>) message.getThumbnails();
//
//                // If thumbnails exist, get smallest (first) thumbnail and display it in the message
//                if (thumbnails.size() > 0) {
//                    ImageUtils.displayImageFromUrl(context, thumbnails.get(0).getUrl(), fileThumbnailImage, fileThumbnailImage.getDrawable());
//                }
//            }
//
//            if (listener != null) {
//                itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        listener.onFileMessageItemClick(message);
//                    }
//                });
//            }
//        }
    }

    private class OtherVideoFileMessageHolder extends RecyclerView.ViewHolder {

        TextView timeText, nicknameText, readReceiptText, dateText;
        ImageView profileImage, fileThumbnailImage;

        public OtherVideoFileMessageHolder(View itemView) {
            super(itemView);

            timeText = (TextView) itemView.findViewById(R.id.text_group_chat_time);
            nicknameText = (TextView) itemView.findViewById(R.id.text_group_chat_nickname);
            fileThumbnailImage = (ImageView) itemView.findViewById(R.id.image_group_chat_file_thumbnail);
            profileImage = (ImageView) itemView.findViewById(R.id.image_group_chat_profile);
            readReceiptText = (TextView) itemView.findViewById(R.id.text_group_chat_read_receipt);
            dateText = (TextView) itemView.findViewById(R.id.text_group_chat_date);
        }

//        void bind(Context context, final FileMessage message, GroupChannel channel, boolean isNewDay, boolean isContinuous, final OnItemClickListener listener) {
//            timeText.setText(DateUtils.formatTime(message.getCreatedAt()));
//
//            // Since setChannel is set slightly after adapter is created, check if null.
//            if (channel != null) {
//                int readReceipt = channel.getReadReceipt(message);
//                if (readReceipt > 0) {
//                    readReceiptText.setVisibility(View.VISIBLE);
//                    readReceiptText.setText(String.valueOf(readReceipt));
//                } else {
//                    readReceiptText.setVisibility(View.INVISIBLE);
//                }
//            }
//
//            // Show the date if the message was sent on a different date than the previous message.
//            if (isNewDay) {
//                dateText.setVisibility(View.VISIBLE);
//                dateText.setText(DateUtils.formatDate(message.getCreatedAt()));
//            } else {
//                dateText.setVisibility(View.GONE);
//            }
//
//            // Hide profile image and nickname if the previous message was also sent by current sender.
//            if (isContinuous) {
//                profileImage.setVisibility(View.INVISIBLE);
//                nicknameText.setVisibility(View.GONE);
//            } else {
//                profileImage.setVisibility(View.VISIBLE);
//                ImageUtils.displayRoundImageFromUrl(context, message.getSender().getProfileUrl(), profileImage);
//
//                nicknameText.setVisibility(View.VISIBLE);
//                nicknameText.setText(message.getSender().getNickname());
//            }
//
//            // Get thumbnails from FileMessage
//            ArrayList<FileMessage.Thumbnail> thumbnails = (ArrayList<FileMessage.Thumbnail>) message.getThumbnails();
//
//            // If thumbnails exist, get smallest (first) thumbnail and display it in the message
//            if (thumbnails.size() > 0) {
//                ImageUtils.displayImageFromUrl(context, thumbnails.get(0).getUrl(), fileThumbnailImage, fileThumbnailImage.getDrawable());
//            }
//
//            if (listener != null) {
//                itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        listener.onFileMessageItemClick(message);
//                    }
//                });
//            }
//        }
    }
}
