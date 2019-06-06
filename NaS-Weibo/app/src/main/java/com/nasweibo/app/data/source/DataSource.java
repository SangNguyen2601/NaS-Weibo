package com.nasweibo.app.data.source;


public interface DataSource {

    interface LoadDataListener<T> {
        void onDataLoaded(T data);

        void onDataNotAvailable();

        void onError(String cause);
    }

    void getContactList(LoadDataListener listener);

    void deleteContact(String groupName, String uid);

}
