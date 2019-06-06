package com.nasweibo.app.contact.Search;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.nasweibo.app.R;
import com.nasweibo.app.chat.ChatActivity;
import com.nasweibo.app.contact.DataBase.ContactDao;
import com.nasweibo.app.contact.DataBase.MapUtils;
import com.nasweibo.app.data.People;

import java.util.ArrayList;
import java.util.List;

public class SearchContactActivity extends AppCompatActivity implements SearchContactAdapter.ItemSearchClick {

  EditText editText;
  RecyclerView recyclerView;
  SearchContactAdapter adapter;

  List<People> peopleList = new ArrayList<>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_search_contact);

    initLayout();
    peopleList = MapUtils.map2(ContactDao.getInstance(this).getAllContact());
    adapter = new SearchContactAdapter(peopleList, this);
    recyclerView.setAdapter(adapter);
  }

  public void initLayout(){
    editText = findViewById(R.id.txt_search);
    recyclerView = findViewById(R.id.recycler_search);
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setClipToPadding(false);
    recyclerView.setHasFixedSize(true);

    editText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }

      @Override
      public void afterTextChanged(Editable editable) {
        search(editable.toString());
      }
    });
  }

  private Handler handler = new Handler();
  private static final Object LOCK = new Object();

  public void search(final String search){
    handler.removeCallbacksAndMessages(null);
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        synchronized (LOCK) {
            adapter.getFilter().filter(search);
        }
      }
    }, 400);
  }

  @Override
  public void onItemSearchClick(People people) {
    Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
    intent.putExtra("friend", people);
    startActivity(intent);
  }
}
