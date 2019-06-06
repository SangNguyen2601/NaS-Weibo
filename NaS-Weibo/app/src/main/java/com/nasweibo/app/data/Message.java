package com.nasweibo.app.data;


public class Message {

    public static final String SENDING_STATUS = "sending";
    public static final String SENT_STATUS = "sent";
    public static final String RECEIVED_STATUS = "delivered";
    public static final String SEEN_STATUS = "seen";
    public static final String FAILED_STATUS = "failed";

    public static final String TEXT_TYPE = "text";
    public static final String URL_TYPE = "link";
    public static final String IMAGE_TYPE = "image";

    private String idReceiver;
    private String idSender;
    private String type;
    private String value;
    private String text;
    private String status;
    private long timestamp;
    private boolean isTempMessage;
    private String id;

    public Message(String text) {
        this.type = TEXT_TYPE;
        this.value = text;
        this.timestamp = System.currentTimeMillis();
        this.status = SENT_STATUS;
    }

    public Message() {
    }

    public void reverseSender(Message message){
        this.idReceiver = message.getIdSender();
        this.idSender = message.getIdReceiver();
        this.type = message.getType();
        this.value = message.getValue();
        this.text = message.getText();
        this.status = message.getStatus();
        this.timestamp = message.getTimestamp();
        this.isTempMessage = message.isTempMessage();
        this.id = message.getId();
    }

    public String getIdReceiver() {
        return idReceiver;
    }

    public void setIdReceiver(String idReceiver) {
        this.idReceiver = idReceiver;
    }

    public String getIdSender() {
        return idSender;
    }

    public void setIdSender(String idSender) {
        this.idSender = idSender;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isTempMessage() {
        return isTempMessage;
    }

    public void setTempMessage(boolean tempMessage) {
        isTempMessage = tempMessage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
