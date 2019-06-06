package com.nasweibo.app.data.source;

public class DataManagement implements DataSource {

    private DataSource localData;
    private DataSource remoteData;

    public DataManagement(DataSource localData, DataSource remoteData){
        this.localData = localData;
        this.remoteData = remoteData;
    }

    @Override
    public void getContactList(LoadDataListener listener) {
        remoteData.getContactList(listener);
    }

    @Override
    public void deleteContact(String groupName, String uid) {
        remoteData.deleteContact(groupName, uid);
    }
}
