package com.nasweibo.app.settings;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nasweibo.app.MainActivity;
import com.nasweibo.app.R;
import com.nasweibo.app.data.User;
import com.nasweibo.app.ui.BaseTabFragment;
import com.nasweibo.app.util.AccountUtils;
import com.nasweibo.app.util.Constant;
import com.nasweibo.app.util.ImageUtils;
import com.thefinestartist.finestwebview.FinestWebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.nasweibo.app.util.LogUtils.LOGD;


public class SettingFragment extends BaseTabFragment implements SettingContract.View, View.OnClickListener {

    RecyclerView recyclerView;
    ImageView avatar;
    TextView username;
    SettingAdapter settingAdapter;
    RelativeLayout cover;
    TextView emptyMsg;

    private static String log_out_label;
    private static String email_label;
    private static String privacy_terms_label;
    private SettingContract.Presenter presenter;
    private static final int PICK_IMAGE = 3105;
    FirebaseStorage storage;
    DatabaseReference mDatabase;
    JSONObject userProfile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPresenter();
        storage = FirebaseStorage.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userProfile = AccountUtils.getUserProfile(getContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_setting, container, false);

        avatar = root.findViewById(R.id.img_avatar);
        avatar.setOnClickListener(this);
        cover = root.findViewById(R.id.cover);
        recyclerView = root.findViewById(R.id.info_recycler_view);
        emptyMsg = root.findViewById(R.id.tv_empty_msg);
        username = root.findViewById(R.id.tv_username);
        username.setOnClickListener(this);

        User profile = presenter.getUserProfile();
        if (profile != null) {

            username.setText(profile.getName());

            ImageUtils.displayImageFromUrl(getContext(), profile.getAvatar()
                    , avatar, getResources().getDrawable(R.drawable.user_default));

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(root.getContext());
            List<SettingItem> listInfo = initSettingList(getContext());
            settingAdapter = new SettingAdapter(listInfo);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(settingAdapter);

        } else {
            cover.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
            emptyMsg.setVisibility(View.VISIBLE);
        }

        return root;
    }

    private List<SettingItem> initSettingList(Context context) {
        List<SettingItem> listRow = new ArrayList<>();
        User profile = presenter.getUserProfile();
        email_label = context.getResources().getString(R.string.email_label);
        SettingItem rEmail = new SettingItem(email_label, profile.getEmail(), R.drawable.ic_profile);
        listRow.add(rEmail);

        log_out_label = context.getResources().getString(R.string.logout_label);
        String logoutDes = context.getResources().getString(R.string.logout_des);
        SettingItem logout = new SettingItem(log_out_label, logoutDes, R.drawable.ic_log_out);
        listRow.add(logout);

        /*privacy_terms_label = context.getResources().getString(R.string.privacy_label);
        String privacy = context.getResources().getString(R.string.privacy_des);
        SettingItem privacySetting = new SettingItem(privacy_terms_label, privacy, R.drawable.ic_info);
        listRow.add(privacySetting);*/

        return listRow;
    }

    private void initPresenter() {
        presenter = new SettingPresenter(this);
    }

    @Override
    public void showProfile(User profile) {

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.img_avatar) {
            new MaterialDialog.Builder(getContext())
                    .items(R.array.edit_profile_action)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            if (text.equals(getString(R.string.change_image_profile))) {
                                Intent intent = new Intent();
                                intent.setType("image/*");
                                intent.setAction(Intent.ACTION_PICK);
                                startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture_gallery)), PICK_IMAGE);
                            }

                            if (text.equals(getString(R.string.edit_user_name))) {
                                View vewInflater = LayoutInflater.from(getContext())
                                        .inflate(R.layout.dialog_edit_username,  (ViewGroup) getView(), false);
                                final EditText input = vewInflater.findViewById(R.id.edit_username);
                                try {
                                    input.setText((String)userProfile.get(AccountUtils.USER_NAME));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                new AlertDialog.Builder(getContext())
                                        .setTitle("Edit username")
                                        .setView(vewInflater)
                                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                String newName = input.getText().toString();
                                                username.setText(newName);
                                                try {
                                                    mDatabase.child("users").child(userProfile.getString(AccountUtils.FIREBASE_UID)).child("name").setValue(newName);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                AccountUtils.setUserValue(getContext(), AccountUtils.USER_NAME, newName);
                                                dialogInterface.dismiss();
                                            }
                                        })
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        }).show();
                            }
                        }
                    })
                    .show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(getContext(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();
                return;
            }
            try {
                InputStream inputStream = getContext().getContentResolver().openInputStream(data.getData());
                updateAvatar(inputStream);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateAvatar(InputStream fis) {
        StorageReference storageRef = storage.getReference();
        StorageReference riversRef = null;
        try {
            riversRef = storageRef.child("images/" + "avatar_" + userProfile.getString(AccountUtils.FIREBASE_UID));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        UploadTask uploadTask = riversRef.putStream(fis);
        Toast.makeText(getContext(), R.string.wait_few_second_update_image, Toast.LENGTH_LONG).show();
        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                LOGD("Firebase upload", "FAILED");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                ImageUtils.displayImageFromUrl(getContext(), downloadUrl.toString()
                        , avatar, getResources().getDrawable(R.drawable.user_default));
                AccountUtils.setUserValue(getContext(), AccountUtils.USER_AVATAR, downloadUrl.toString());
            }
        });
    }

    /**
     * Adapter generate setting list item
     */
    public class SettingAdapter extends RecyclerView.Adapter<SettingAdapter.ViewHolder> {
        private List<SettingItem> infoList;

        public SettingAdapter(List<SettingItem> infoList) {
            this.infoList = infoList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_setting_layout, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final SettingItem config = infoList.get(position);
            holder.label.setText(config.getLabel());
            holder.value.setText(config.getValue());
            holder.icon.setImageResource(config.getIcon());
            holder.itemView.setTag(config.getLabel());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (view.getTag() == log_out_label) {
                        new MaterialDialog.Builder(getContext())
                                .title(R.string.logout_label)
                                .positiveText(R.string.yes)
                                .negativeText(R.string.no)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        presenter.logout();
                                        getActivity().startActivity(new Intent(getContext(), MainActivity.class));
                                        getActivity().finish();
                                    }
                                })
                                .show();
                        return;
                    }

                    /*if (view.getTag() == privacy_terms_label) {
                        new FinestWebView.Builder(getContext()).show(Constant.PRIVACY_TERMS_URL);
                    }*/

                }
            });
        }

        @Override
        public int getItemCount() {
            return infoList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView label, value;
            public ImageView icon;

            public ViewHolder(View view) {
                super(view);
                label = view.findViewById(R.id.tv_title);
                value = view.findViewById(R.id.tv_detail);
                icon = view.findViewById(R.id.img_icon);
            }
        }

    }
}
