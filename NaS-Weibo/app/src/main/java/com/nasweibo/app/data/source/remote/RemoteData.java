package com.nasweibo.app.data.source.remote;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nasweibo.app.R;
import com.nasweibo.app.data.ContactGroup;
import com.nasweibo.app.data.People;
import com.nasweibo.app.data.User;
import com.nasweibo.app.data.source.DataSource;
import com.nasweibo.app.util.AccountUtils;
import com.nasweibo.app.util.Constant;

import java.util.ArrayList;
import java.util.List;


public class RemoteData implements DataSource {

    private Context mContext;
    private List<People> tmpPeopleList = new ArrayList<>();
    private List<People> listPeopleID = new ArrayList<>();
    private List<ContactGroup> contactGroupList = new ArrayList<>();
    DatabaseReference mDatabase;

    public RemoteData(Context context) {
        this.mContext = context;
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void getContactList(final LoadDataListener listener) {
        tmpPeopleList.clear();
        listPeopleID.clear();
        contactGroupList.clear();

        FirebaseDatabase.getInstance().getReference()
                .child(Constant.FRIEND_PREF).child(AccountUtils.getUID(mContext))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {

                            for (DataSnapshot dataGroup : dataSnapshot.getChildren()) {
                                String groupName = dataGroup.getKey();
                                ContactGroup contactGroup = new ContactGroup();
                                contactGroup.setGroupName(groupName);

                                final List<People> peopleList = new ArrayList<>();
                                for (DataSnapshot dataFriends : dataGroup.getChildren()) {
                                    String friendID = dataFriends.getKey();
                                    People people = new People();
                                    people.setUid(friendID);
                                    peopleList.add(people);
                                    listPeopleID.add(people);
                                }

                                contactGroup.setListPeople(peopleList);
                                contactGroupList.add(contactGroup);
                            }

                            getUserInfo(0, listener);
                        } else {
                            listener.onDataNotAvailable();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        String msg = mContext.getResources().getString(R.string.load_data_failed);
                        listener.onError(msg);
                    }
                });
    }

    @Override
    public void deleteContact(final String groupName, final String uid) {
        mDatabase.child("friend").child(AccountUtils.getUID(mContext))
                .child(groupName).child(uid).setValue(null);
    }

    private void getUserInfo(final int index, final LoadDataListener listener) {
        if (listPeopleID.size() == 0) {
            return;
        }
        if (index == listPeopleID.size()) {
            contactGroupList = mergePeopleToGroup();
            listener.onDataLoaded(contactGroupList);
        } else {
            String friendId = listPeopleID.get(index).getUid();
            FirebaseDatabase.getInstance().getReference()
                    .child(Constant.USER_PREF).child(friendId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot != null) {
                                User friend = dataSnapshot.getValue(User.class);
                                People people = new People(friend);
                                tmpPeopleList.add(people);
                            }

                            getUserInfo(index + 1, listener);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }

    private List<ContactGroup> mergePeopleToGroup() {
        List<ContactGroup> mergingGroup = new ArrayList<>();
        for (ContactGroup group : contactGroupList) {
            List<People> listPeople = group.getListPeople();
            List<People> mergingPeopleList = new ArrayList<>();
            for (People people : tmpPeopleList) {
                if (listPeople.contains(people) && !mergingPeopleList.contains(people)) {
                    mergingPeopleList.add(people);
                }
            }
            group.setListPeople(mergingPeopleList);
            mergingGroup.add(group);
        }

        return mergingGroup;
    }
}
