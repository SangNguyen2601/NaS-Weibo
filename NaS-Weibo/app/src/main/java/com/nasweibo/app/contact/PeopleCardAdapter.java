package com.nasweibo.app.contact;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nasweibo.app.R;
import com.nasweibo.app.chat.ChatActivity;
import com.nasweibo.app.data.People;
import com.nasweibo.app.util.ImageUtils;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;


public class PeopleCardAdapter extends RecyclerView.Adapter<PeopleCardAdapter.PeopleCardVH> {

    Context mContext;
    List<People> peoples;
    ItemMenuMoreClick itemMenuMoreClick;
    String groupName;

    public PeopleCardAdapter(Context context, List<People> peopleList,
                             ItemMenuMoreClick itemMenuMoreClick, String groupName) {
        this.mContext = context;
        this.peoples = peopleList;
        this.itemMenuMoreClick = itemMenuMoreClick;
        this.groupName = groupName;
    }

    @Override
    public PeopleCardVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card_contact, parent, false);
        return new PeopleCardVH(root);
    }

    @Override
    public void onBindViewHolder(PeopleCardVH holder, int position) {
        final People people = peoples.get(position);
        ImageUtils.displayImageFromUrl(mContext, people.getAvatar(),
                holder.avatar, mContext.getResources().getDrawable(R.drawable.user_default));

        long period = 0;

        if (people.getStatus() != null) {
            long lastUpdateOnline = people.getStatus().getOnlinestamp();
            long currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
            period = currentTime - lastUpdateOnline;
        }

        long diffMinute = TimeUnit.MILLISECONDS.toMinutes(period);

        if (diffMinute <= 1 && period != 0) {
            holder.status.setVisibility(View.VISIBLE);
            holder.onlineStamp.setText("Active now");
            holder.onlineStamp.setVisibility(View.VISIBLE);
        } else if (period != 0) {
            holder.status.setVisibility(View.GONE);
            long minute = diffMinute;
            int minutePrety = (int) minute;
            if (minutePrety == 0) minutePrety = 1;
            if (minutePrety < 60) {
                holder.onlineStamp.setText("Active " + minutePrety + "m ago");
                holder.onlineStamp.setVisibility(View.VISIBLE);
            } else {
                if (minutePrety < 60 * 24) {
                    int hour = (int) (TimeUnit.MILLISECONDS.toHours(period));
                    holder.onlineStamp.setText("Active " + hour + "h ago");
                    holder.onlineStamp.setVisibility(View.VISIBLE);
                } else holder.onlineStamp.setVisibility(View.GONE);
            }
        } else {
            holder.status.setVisibility(View.GONE);
            holder.onlineStamp.setVisibility(View.GONE);
        }
        holder.contactName.setText(people.getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("friend", people);
                mContext.startActivity(intent);
            }
        });

        holder.btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(mContext, view);
                popup.inflate(R.menu.people_card_popup);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.item_delete:
                                if (itemMenuMoreClick != null)
                                    itemMenuMoreClick.onItemDeleteClick(groupName, people);
                                break;
                            case R.id.item_block:
                                if (itemMenuMoreClick != null)
                                    itemMenuMoreClick.onItemBlockClick(groupName, people);
                                break;
                        }
                        return false;
                    }
                });
                popup.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return peoples.size();
    }

    public class PeopleCardVH extends RecyclerView.ViewHolder {

        ImageView avatar, status, btnOptions;
        TextView contactName, onlineStamp;

        public PeopleCardVH(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.imv_avatar);
            status = itemView.findViewById(R.id.user_status);
            btnOptions = itemView.findViewById(R.id.btn_options);
            contactName = itemView.findViewById(R.id.contact_name);
            onlineStamp = itemView.findViewById(R.id.online_stamp);
        }
    }

    public interface ItemMenuMoreClick {
        void onItemDeleteClick(String groupName, People people);

        void onItemBlockClick(String groupName, People people);
    }
}
