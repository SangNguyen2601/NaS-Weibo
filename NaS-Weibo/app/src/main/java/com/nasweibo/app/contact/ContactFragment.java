package com.nasweibo.app.contact;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.nasweibo.app.R;
import com.nasweibo.app.contact.Search.SearchContactActivity;
import com.nasweibo.app.data.ContactGroup;
import com.nasweibo.app.data.People;
import com.nasweibo.app.data.source.DataManagement;
import com.nasweibo.app.data.source.local.LocalData;
import com.nasweibo.app.data.source.remote.RemoteData;
import com.nasweibo.app.ui.BaseTabFragment;
import com.nasweibo.app.ui.widget.recyclerview.ItemMarginDecoration;
import com.nasweibo.app.util.Config;

import java.util.ArrayList;
import java.util.List;


public class ContactFragment extends BaseTabFragment implements ContactFragContract.View, PeopleCardAdapter.ItemMenuMoreClick {

    RecyclerView groupContactList;
    CircularProgressView waitingProgress;
    TextView emptyMsg;
    private DataManagement dataManagement;
    private List<ContactGroup> contactGroupList;
    private ContactGroupAdapter contactGroupAdapter;
    private ContactFragContract.Presenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalData localData = new LocalData();
        RemoteData remoteData = new RemoteData(getContext());
        dataManagement = new DataManagement(localData, remoteData);
        presenter = new ContactFragPresenter(this, dataManagement);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_contact, container, false);
        groupContactList = root.findViewById(R.id.recycler_group_contact);
        groupContactList.setHasFixedSize(true);
        int coursesVerticalMargin = getResources().getDimensionPixelSize(R.dimen.spacing_micro);
        groupContactList.addItemDecoration(new ItemMarginDecoration(0, coursesVerticalMargin,
                0, coursesVerticalMargin));

        emptyMsg = root.findViewById(R.id.contact_empty);
        waitingProgress = root.findViewById(R.id.load_contact_progress);

        //TODO load contact list from local data;
        contactGroupList = new ArrayList<>();
        contactGroupAdapter = new ContactGroupAdapter(getContext(), contactGroupList, this);
        groupContactList.setAdapter(contactGroupAdapter);

        FloatingActionButton fab = root.findViewById(R.id.fab_add_contact);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), FriendRequestActivity.class);
                startActivity(intent);
            }
        });

        showWaitingProcess();
        root.findViewById(R.id.layout_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SearchContactActivity.class);
                startActivity(intent);
            }
        });
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        //TODO refresh contact list
        if (!Config.DEBUG) {
            presenter.start();
        }
    }

    @Override
    public void showWaitingProcess() {
        waitingProgress.setVisibility(View.VISIBLE);
        waitingProgress.startAnimation();
    }

    @Override
    public void stopWaitingProcess() {
        waitingProgress.stopAnimation();
        waitingProgress.setVisibility(View.INVISIBLE);
    }


    @Override
    public void showContactList(List<ContactGroup> contactGroups) {
        stopWaitingProcess();
        if (contactGroups.isEmpty()) {
            showEmptyMsg();
        }else {
            hideEmptyMsg();
        }
        contactGroupList.clear();
        contactGroupList.addAll(contactGroups);
        contactGroupAdapter.notifyDataSetChanged();
    }

    @Override
    public Context getContextView() {
        return getContext();
    }

    public void showBackgroundMsg(String msg){
        emptyMsg.setVisibility(View.VISIBLE);
        emptyMsg.setText(msg);
    }

    public void showEmptyMsg() {
        emptyMsg.setVisibility(View.VISIBLE);
        String msg = getString(R.string.contact_empty_msg);
        emptyMsg.setText(msg);
    }

    public void hideEmptyMsg() {
        emptyMsg.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onItemDeleteClick(String groupName, People people) {
        presenter.delete(groupName, people);
    }

    @Override
    public void onItemBlockClick(String groupName, People people) {
        presenter.block(groupName, people);
    }

    public class ContactGroupAdapter extends RecyclerView.Adapter<ContactGroupAdapter.ContactGroupVH> {

        List<ContactGroup> contactGroupList;
        Context mContext;
        PeopleCardAdapter.ItemMenuMoreClick itemMenuMoreClick;

        public ContactGroupAdapter(Context context, List<ContactGroup> contactGroups, PeopleCardAdapter.ItemMenuMoreClick itemMenuMoreClick) {
            this.contactGroupList = contactGroups;
            this.mContext = context;
            this.itemMenuMoreClick = itemMenuMoreClick;
        }

        @Override
        public ContactGroupVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_contact_group, parent, false);

            return new ContactGroupVH(itemView);
        }

        @Override
        public void onBindViewHolder(ContactGroupVH holder, int position) {
            ContactGroup contactGroup = contactGroupList.get(position);
            holder.groupName.setText(contactGroup.getGroupName());
            String size = contactGroup.getListPeople().size() + " " + getResources().getString(R.string.group_items_unit);
            holder.groupSize.setText(size);

            PeopleCardAdapter adapter = new PeopleCardAdapter(mContext, contactGroup.getListPeople(),
                    itemMenuMoreClick, contactGroup.getGroupName());
            holder.contactList.setAdapter(adapter);

        }

        @Override
        public int getItemCount() {
            return contactGroupList.size();
        }

        class ContactGroupVH extends RecyclerView.ViewHolder {

            TextView groupName;
            TextView groupSize;
            RecyclerView contactList;

            public ContactGroupVH(View itemView) {
                super(itemView);
                groupName = itemView.findViewById(R.id.group_name);
                groupSize = itemView.findViewById(R.id.number_people);
                contactList = itemView.findViewById(R.id.listCardContact);

            }
        }
    }

    @Override
    public void onDestroyView() {
        presenter.stopUpdateStateOnline();
        super.onDestroyView();
    }
}
