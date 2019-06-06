package com.nasweibo.app.contact;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.nasweibo.app.R;
import com.nasweibo.app.data.User;


public class FriendRequestActivity extends AppCompatActivity implements FriendRequestContract.View{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finder);
        this.setFinishOnTouchOutside(true);

        FriendRequestPresenter presenter = new FriendRequestPresenter(this);
        presenter.start();
    }
    @Override
    public void showSearchFragment() {
        FinderFragment finderFragment = new FinderFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.add_contact_frame, finderFragment);
        transaction.commit();
    }

    @Override
    public void showPreviewContact(User friend) {
        PreviewFragment previewFragment = PreviewFragment.newInstance(friend);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.add_contact_frame, previewFragment);
        transaction.commit();
    }

    /*
    * Friends
    Family
    Co-worker
    Acquitances
    * */
}
