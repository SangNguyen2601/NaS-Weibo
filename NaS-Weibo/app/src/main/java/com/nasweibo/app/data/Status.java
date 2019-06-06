package com.nasweibo.app.data;

import java.io.Serializable;


public class Status implements Serializable {

    private boolean online;
    private long onlinestamp;

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public long getOnlinestamp() {
        return onlinestamp;
    }

    public void setOnlinestamp(long onlinestamp) {
        this.onlinestamp = onlinestamp;
    }
}
