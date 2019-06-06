package com.nasweibo.app.contact;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nasweibo.app.R;
import com.nasweibo.app.data.User;
import com.nasweibo.app.util.AccountUtils;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FinderFragment extends Fragment {

    TextView btnAddContact, tvHintEmail;
    LinearLayout blockEmailSearch;
    EditText editEmail;
    CircularProgressView progressbar;
    FriendRequestActivity parentActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.parentActivity = (FriendRequestActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search_contact, container, false);
        btnAddContact = root.findViewById(R.id.btn_add_contact);
        blockEmailSearch = root.findViewById(R.id.block_email_search);
        editEmail = root.findViewById(R.id.et_email);
        tvHintEmail = root.findViewById(R.id.hint_input_msg);
        progressbar = root.findViewById(R.id.waiting_search_contact);
        btnAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editEmail.getText().toString();
                searchUserByEmail(email);
            }
        });
        return root;
    }

    private void searchUserByEmail(String email) {
        Pattern VALID_EMAIL_ADDRESS_REGEX =
                Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        if (matcher.find()) {
            showWaiting();
            FirebaseDatabase.getInstance().getReference().child("users")
                    .orderByChild("email").equalTo(email)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                String id = ((HashMap) dataSnapshot.getValue()).keySet().iterator().next().toString();
                                if (id.equals(AccountUtils.getUID(getContext()))) {
                                    //show msg error
                                    tvHintEmail.setTextColor(getResources().getColor(R.color.red_500));
                                    tvHintEmail.setText(R.string.cannot_add_yourself_msg);
                                } else {

                                    HashMap userMap = (HashMap) ((HashMap) dataSnapshot.getValue()).get(id);
                                    User user = new User();
                                    user.setName((String) userMap.get(AccountUtils.USER_NAME));
                                    user.setEmail((String) userMap.get(AccountUtils.USER_EMAIL));
                                    user.setAvatar((String) userMap.get(AccountUtils.USER_AVATAR));
                                    user.setUid(id);

                                    parentActivity.showPreviewContact(user);
                                }
                            }else {
                                progressbar.stopAnimation();
                                progressbar.setVisibility(View.INVISIBLE);
                                tvHintEmail.setTextColor(getResources().getColor(R.color.red_500));
                                tvHintEmail.setText(R.string.user_not_exist);
                            }

                            stopWaiting();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            tvHintEmail.setTextColor(getResources().getColor(R.color.red_500));
                            tvHintEmail.setText(R.string.server_internal_error);
                            stopWaiting();
                        }
                    });
        } else {
            tvHintEmail.setTextColor(getResources().getColor(R.color.red_500));
            tvHintEmail.setText(R.string.email_invalid_msg);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //make callback point to empty
    }

    private void showWaiting(){
        progressbar.setVisibility(View.VISIBLE);
        progressbar.startAnimation();
        btnAddContact.setVisibility(View.INVISIBLE);
        blockEmailSearch.setVisibility(View.INVISIBLE);
    }

    private void stopWaiting(){
        progressbar.stopAnimation();
        progressbar.setVisibility(View.INVISIBLE);
        btnAddContact.setVisibility(View.VISIBLE);
        blockEmailSearch.setVisibility(View.VISIBLE);
    }

}
