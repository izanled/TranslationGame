package com.izanled.translation.game.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.izanled.translation.game.R;
import com.izanled.translation.game.common.CommonData;
import com.izanled.translation.game.data.UserData;
import com.izanled.translation.game.utils.ToastManager;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StartActivity extends AppCompatActivity {
    private static final String TAG = StartActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 123;
    List<AuthUI.IdpConfig> providers = null;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);
        db = FirebaseFirestore.getInstance();
        // Choose authentication providers
        providers = Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build());
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(), RC_SIGN_IN);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                db.collection(CommonData.COLLECTION_USERS).whereEqualTo(CommonData.FIELD_EMAIL, user.getEmail()).get().addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        UserData sing_user = null;
                        for (QueryDocumentSnapshot document : task.getResult()){
                            sing_user = document.toObject(UserData.class);
                            sing_user.set_id(document.getId());
                            CommonData.getInstance().setCurUser(sing_user);
                        }
                        if(sing_user == null){
                            // 신규유저
                            newUserCrate(user.getEmail());
                        }else{
                            ToastManager.getInstance().showLongTosat(user.getEmail()+ " "+ getString(R.string.msg_login_id));
                            startActivity(new Intent(StartActivity.this, MainActivity.class));
                            finish();
                        }
                    }else{
                        ToastManager.getInstance().showLongTosat(getString(R.string.msg_not_found_id));
                    }
                });

                // ...
            } else {
                ToastManager.getInstance().showLongTosat(getString(R.string.msg_fail_login));
                response.getError().printStackTrace();
                Log.d(TAG, "에러 코드 : " + response.getError().getErrorCode());
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

    public void getUserData(String email){
        db.collection(CommonData.COLLECTION_USERS).whereEqualTo(CommonData.FIELD_EMAIL, email).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                UserData sing_user = null;
                for (QueryDocumentSnapshot document : task.getResult()){
                    sing_user = document.toObject(UserData.class);
                    sing_user.set_id(document.getId());
                    CommonData.getInstance().setCurUser(sing_user);
                }
                if(sing_user != null){
                    ToastManager.getInstance().showLongTosat(email+ " "+ getString(R.string.msg_login_id));
                    startActivity(new Intent(StartActivity.this, MainActivity.class));
                    finish();
                }
            }else{
                ToastManager.getInstance().showLongTosat(getString(R.string.msg_not_found_id));
            }
        });

    }

    public void newUserCrate(String email){
        UserData newUser = new UserData(email, 1000);
        db.collection(CommonData.COLLECTION_USERS).document().set(newUser).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                getUserData(email);
            }else{
                ToastManager.getInstance().showLongTosat(getString(R.string.msg_fail_create_id));
            }
        });
    }

    @OnClick(R.id.btn_login)
    public void OnClick(View view){
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(), RC_SIGN_IN);
    }

    @BindView(R.id.btn_login)    Button btn_login;
}
