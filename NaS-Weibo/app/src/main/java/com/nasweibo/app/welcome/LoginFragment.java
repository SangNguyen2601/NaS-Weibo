package com.nasweibo.app.welcome;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nasweibo.app.R;
import com.nasweibo.app.data.User;
import com.nasweibo.app.util.AccountUtils;
import com.nasweibo.app.util.Config;
import com.nasweibo.app.worker.HttpWorker;

import static com.nasweibo.app.util.LogUtils.LOGD;
import static com.nasweibo.app.util.LogUtils.LOGE;
import static com.nasweibo.app.util.LogUtils.makeLogTag;



/**
 * Facebook: crazymaxin007@gmail.com
 * Pass: monu8586%$#@!
 * Gmail: crazycastorin@gmail.com
 * Pass: godhan%$#@!
 */
public class LoginFragment extends WelcomeFragment implements WelcomeActivity.WelcomeContent {

    private static final String TAG = makeLogTag(LoginFragment.class);
    private LoginButton loginButton;

    private CallbackManager callbackManager;
    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 3105;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    MaterialDialog waitingDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.gl_oauth_client_type_3))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_login, container, false);
        loginButton = root.findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        loginButton.setFragment(this);
        loginButton.setHeight(56);
        loginButton.setTextSize(18);

        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken token = loginResult.getAccessToken();
                if (token != null) {
                    LOGD(TAG, "Login successfully, save fid = " + token.getUserId());
                    handleFacebookAccessToken(token);
                    startWaitingDialog();
                }else {
                    LOGE(TAG, "Login failed");
                }
            }

            @Override
            public void onCancel() {
                doFinish();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getContext(), "Login Error, please try agian", Toast.LENGTH_LONG).show();
            }
        });

        // Set the dimensions of the sign-in button.
        SignInButton signInButton = root.findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignIn();
            }
        });

        return root;
    }

    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        if(waitingDialog != null){
            waitingDialog.cancel();
        }
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            try {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
                startWaitingDialog();
            } catch (ApiException e) {
                e.printStackTrace();
                LOGE(TAG, e.getMessage());
            }

        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            AccountUtils.addSettingValue(getContext(), AccountUtils.ACCOUNT_TYPE, AccountUtils.GOOGLE_ACC);
            if (account != null) {
                Uri photoUri = account.getPhotoUrl();
                if (photoUri != null) {
                    String url = photoUri.getScheme() + ":" + photoUri.getSchemeSpecificPart() + "?sz=200";
                    HttpWorker.downloadAvatarOnline(getContext(), url);
                }
                AccountUtils.setUserValue(getContext(), AccountUtils.USER_NAME, account.getDisplayName());
                AccountUtils.setUserValue(getContext(), AccountUtils.USER_EMAIL, account.getEmail());
            }
            doNext();
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    @Override
    public boolean shouldDisplay(Context context) {
        if(Config.DEBUG){
            return false;
        }

        if(mAuth == null){
            mAuth = FirebaseAuth.getInstance();
        }
        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null){
            return true;
        }

        return false;
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if(user != null){
                                saveUserInfo(user);
                                AccountUtils.saveUID(getContext(), user.getUid());
                            }else {
                                LOGE(TAG, "sign with Google firebase error.");
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getContext(),R.string.signin_credential_false, Toast.LENGTH_LONG ).show();
                        }
                        doNext();
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if(user != null){
                                saveUserInfo(user);
                                AccountUtils.saveUID(getContext(), user.getUid());
                            }else {
                                LOGE(TAG, "sign with Google firebase error.");
                            }
                            doNext();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }


                    }
                });
    }

    private void saveUserInfo(FirebaseUser user){
        if(user == null){
            return;
        }

        User loginUser = new User(user);
        mDatabase.child("users").child(user.getUid()).setValue(loginUser);

        AccountUtils.setUserValue(getContext(), AccountUtils.FIREBASE_UID, user.getUid());
        AccountUtils.setUserValue(getContext(), AccountUtils.USER_NAME, user.getDisplayName());
        AccountUtils.setUserValue(getContext(), AccountUtils.USER_EMAIL, user.getEmail());
        AccountUtils.setUserValue(getContext(), AccountUtils.USER_AVATAR, user.getPhotoUrl().toString());
    }

    private void startWaitingDialog(){
        waitingDialog = new MaterialDialog.Builder(getContext())
                .title(R.string.login_label)
                .content(R.string.waiting_sign_msg)
                .progress(true, 0)
                .cancelable(false)
                .show();
    }

}
