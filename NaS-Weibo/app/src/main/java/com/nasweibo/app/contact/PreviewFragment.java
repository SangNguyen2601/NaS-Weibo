package com.nasweibo.app.contact;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nasweibo.app.R;
import com.nasweibo.app.data.User;
import com.nasweibo.app.util.AccountUtils;
import com.nasweibo.app.util.Constant;
import com.nasweibo.app.util.ImageUtils;

import de.hdodenhof.circleimageview.CircleImageView;


public class PreviewFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    CircleImageView circleImgAvatar;
    TextView tvUsername, tvEmail, btnAddToGroup;
    RelativeLayout blockUserItem;
    Spinner spGroupOptions;

    private DatabaseReference mDatabase;
    private User friend;

    public static PreviewFragment newInstance(User user) {
        PreviewFragment fragment = new PreviewFragment();

        Bundle args = new Bundle();
        args.putSerializable("user", user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_contact_preview, container, false);

        btnAddToGroup = root.findViewById(R.id.btn_add_group);
        btnAddToGroup.setOnClickListener(this);
        blockUserItem = root.findViewById(R.id.block_user_item);
        circleImgAvatar = root.findViewById(R.id.imv_avatar);
        tvUsername = root.findViewById(R.id.tv_username);
        tvEmail = root.findViewById(R.id.tv_email);
        spGroupOptions = root.findViewById(R.id.sp_group);
        spGroupOptions.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.contact_group, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGroupOptions.setAdapter(spinnerAdapter);

        Bundle bundle = getArguments();
        if (bundle != null && bundle.getSerializable("user") != null) {
            this.friend = (User) bundle.getSerializable("user");
            showPreviewUser(friend);
        }
        return root;
    }

    private void showPreviewUser(User user) {
        if (user == null) {
            return;
        }
        ImageUtils.displayImageFromUrl(getContext(), user.getAvatar(),
                circleImgAvatar, getResources().getDrawable(R.drawable.user_default));
        tvUsername.setText(user.getName());
        tvEmail.setText(user.getEmail());
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == btnAddToGroup.getId()) {
            String groupSelected = (String) spGroupOptions.getSelectedItem();
            String myUid = AccountUtils.getUID(getContext());
//            HashMap<String, Boolean> record = new HashMap<>();
//            record.put(friend.getUid(), true);
            mDatabase.child(Constant.FRIEND_PREF).child(myUid).child(groupSelected)
                    .child(friend.getUid()).setValue(true);
            getActivity().finish();
        }
    }
}
