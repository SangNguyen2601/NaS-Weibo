package com.nasweibo.app.contact.Search;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nasweibo.app.R;
import com.nasweibo.app.data.People;
import com.nasweibo.app.util.ImageUtils;

import java.util.ArrayList;
import java.util.List;


public class SearchContactAdapter extends RecyclerView.Adapter<SearchContactAdapter.ViewHolder> {

  List<People> peopleList;
  private List<People> listPeopleAll;
  private List<People> listPeopleSuggestion;

  ItemSearchClick itemSearchClick;

  public SearchContactAdapter(List<People> people, ItemSearchClick itemSearchClick) {
    this.peopleList = people;
    this.itemSearchClick = itemSearchClick;
    this.listPeopleAll = new ArrayList<>(peopleList);
    listPeopleSuggestion = new ArrayList<>();
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_search_people, parent, false);
    return new SearchContactAdapter.ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    final People people = peopleList.get(position);
    holder.txtUserName.setText(people.getName());
    holder.txtUserEmail.setText(people.getEmail());

    ImageUtils.displayImageFromUrl(holder.itemView.getContext(), people.getAvatar(),
        holder.imgAvatar, holder.itemView.getContext().getResources().getDrawable(R.drawable.user_default));

    holder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        itemSearchClick.onItemSearchClick(people);
      }
    });
  }

  @Override
  public int getItemCount() {
    return peopleList.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    ImageView imgAvatar;
    TextView txtUserName;
    TextView txtUserEmail;

    public ViewHolder(View itemView) {
      super(itemView);
      imgAvatar = itemView.findViewById(R.id.imv_avatar);
      txtUserName = itemView.findViewById(R.id.tv_username);
      txtUserEmail = itemView.findViewById(R.id.txt_user_email);
    }
  }

  public interface ItemSearchClick {
    void onItemSearchClick(People people);
  }

  @NonNull
  public Filter getFilter() {
    return mNameFilter;
  }

  private Filter mNameFilter = new Filter() {
    @Override
    public String convertResultToString(Object resultValue) {
      return ((People) (resultValue)).getName();
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
      listPeopleSuggestion.clear();
      if (constraint == null || "".equals(constraint)) {
        listPeopleSuggestion.addAll(listPeopleAll);
      } else {
        for (People area : listPeopleAll) {
          String query = stripAccent(constraint.toString()).toLowerCase();
          String name = stripAccent(area.getName()).toLowerCase();

          if (name.contains(query)) {
            listPeopleSuggestion.add(area);
          }
        }
      }
      FilterResults filterResults = new FilterResults();
      filterResults.values = listPeopleSuggestion;
      filterResults.count = listPeopleSuggestion.size();
      return filterResults;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
      if (null == results || results.count <= 0) {
        peopleList.clear();
        notifyDataSetChanged();
        return;
      }
      ArrayList<People> filteredList = (ArrayList<People>) results.values;
      filteredList = (ArrayList<People>) filteredList.clone();

      peopleList.clear();
      List<People> tobeAdded = new ArrayList<>();
      for (People c : filteredList) {
        tobeAdded.add(c);
      }
      peopleList.addAll(tobeAdded);
      notifyDataSetChanged();
    }
  };

  public static String stripAccent(String s) {
    if (s == null) {
      return null;
    }
    String stripAccent = org.apache.commons.lang3.StringUtils.stripAccents(s);

    stripAccent = replaceSpecialAccent(stripAccent);
    return stripAccent;
  }

  private static String replaceSpecialAccent(String s) {
    String result = s.replaceAll("đ", "d");
    result = result.replaceAll("Đ", "d");
    return result;
  }
}
