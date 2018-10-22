package com.izanled.translation.game.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.izanled.translation.game.R;
import com.izanled.translation.game.common.CommonData;
import com.izanled.translation.game.data.UserData;
import com.izanled.translation.game.service.TranslationService;
import com.izanled.translation.game.utils.ToastManager;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements RewardedVideoAdListener {
    private static final String TAG = MainActivity.class.getName();

    static final int RC_SIGN_IN = 123;
    List<AuthUI.IdpConfig> providers = null;

    static final int REQ_CODE_OVERLAY_PERMISSION = 9999;

    private InterstitialAd mInterstitialAd;
    private RewardedVideoAd mRewardedVideoAd;
    private FirebaseFirestore db;
    FirebaseUser user;

    HelpDialog mHelpDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initLayout();
        initAdMob();
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null){
            providers = Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build());
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(), RC_SIGN_IN);
        }else{
            getUserData(user.getEmail());
        }

    }

    private void initLayout(){
        btn_sign.setSize(SignInButton.SIZE_STANDARD);

        try {
            tv_version.setText(getString(R.string.app_version) + " " + getPackageManager().getPackageInfo(getPackageName(),0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        spinner_source_lang.setSelection(CommonData.getInstance().getSourceLang());
        spinner_target_lang.setSelection(CommonData.getInstance().getTargetLang());

        sw_show_original.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b)
                sw_show_original.setText(R.string.show);
            else
                sw_show_original.setText(R.string.hide);
            CommonData.getInstance().setIsShowOriginal(b);
        });

        sw_show_original.setChecked(CommonData.getInstance().getIsShowOriginal());

        if(CommonData.getInstance().getIsShowOriginal())
            sw_show_original.setText(R.string.show);
        else
            sw_show_original.setText(R.string.hide);

        spinner_source_lang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        CommonData.getInstance().setmSourceLang("zh-CN");
                        break;
                    case 1:
                        CommonData.getInstance().setmSourceLang("zh-TW");
                        break;
                    case 2:
                        CommonData.getInstance().setmSourceLang("ja");
                        break;
                    case 3:
                        CommonData.getInstance().setmSourceLang("en");
                        break;
                    case 4:
                        CommonData.getInstance().setmSourceLang("ko");
                        break;
                }
                CommonData.getInstance().setSourceLang(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        spinner_target_lang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        CommonData.getInstance().setmTargetLang("zh-CN");
                        break;
                    case 1:
                        CommonData.getInstance().setmTargetLang("zh-TW");
                        break;
                    case 2:
                        CommonData.getInstance().setmTargetLang("ja");
                        break;
                    case 3:
                        CommonData.getInstance().setmTargetLang("en");
                        break;
                    case 4:
                        CommonData.getInstance().setmTargetLang("ko");
                        break;
                }
                CommonData.getInstance().setTargetLang(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    /**
     * 광고 초기화
     */
    private void initAdMob(){
        // 베너 광고 초기화
        AdRequest adRequest = new AdRequest.Builder().build();
        ad_view_bottom.loadAd(adRequest);
        ad_view_top.loadAd(adRequest);

        // 전면 광고 초기화
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-8427929259216639/3837255858");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

        // Use an activity context to get the rewarded video instance.
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();
    }

    /**
     * 리워드 광고 초기화
     */
    private void loadRewardedVideoAd() {
        //mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().addTestDevice("84B5C0F73F4B4897C483EC3A2EA1A04C").build());     // 실험 광고
        mRewardedVideoAd.loadAd("ca-app-pub-8427929259216639/8109750770", new AdRequest.Builder().build());
    }

    /**
     * 리워드 광고 리스너
     */
    @Override
    public void onRewarded(RewardItem reward) {
        // Reward the user.
        try {
            db.collection(CommonData.COLLECTION_USERS).document(CommonData.getInstance().getUserDocId()).update("point", CommonData.getInstance().getCurUser().getPoint()+200).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    ToastManager.getInstance().showLongTosat(getString(R.string.success_rewoad));
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        //Toast.makeText(this, "onRewardedVideoAdLeftApplication", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdClosed() {
        //Toast.makeText(this, "onRewardedVideoAdClosed", Toast.LENGTH_SHORT).show();
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
        //Toast.makeText(this, "onRewardedVideoAdFailedToLoad", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        //Toast.makeText(this, "onRewardedVideoAdLoaded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdOpened() {
        //Toast.makeText(this, "onRewardedVideoAdOpened", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoStarted() {
        //Toast.makeText(this, "onRewardedVideoStarted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoCompleted() {
        //Toast.makeText(this, "onRewardedVideoCompleted", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        mRewardedVideoAd.pause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        mRewardedVideoAd.resume(this);
        if(CommonData.getInstance().isServiceRun())
            btn_start.setText(R.string.end);
        else
            btn_start.setText(R.string.start);

        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRewardedVideoAd.destroy(this);
        stopService(new Intent(this, TranslationService.class));
        CommonData.getInstance().setServiceRun(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == RC_SIGN_IN) {
                IdpResponse response = IdpResponse.fromResultIntent(data);

                if (resultCode == RESULT_OK) {
                    // Successfully signed in
                    user = FirebaseAuth.getInstance().getCurrentUser();

                    db.collection(CommonData.COLLECTION_USERS).whereEqualTo(CommonData.FIELD_EMAIL, user.getEmail()).get().addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            UserData sing_user = null;
                            for (QueryDocumentSnapshot document : task.getResult()){
                                sing_user = document.toObject(UserData.class).withId(document.getId());
                                CommonData.getInstance().setCurUser(sing_user);
                            }
                            if(sing_user == null){
                                // 신규유저
                                newUserCrate(user.getEmail());
                            }else{
                                ToastManager.getInstance().showLongTosat(user.getEmail()+ " "+ getString(R.string.msg_login_id));
                                getUserData(user.getEmail());
                            }
                        }else{
                            ToastManager.getInstance().showLongTosat(getString(R.string.msg_not_found_id));
                            btn_sign.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    btn_sign.setVisibility(View.VISIBLE);
                    ToastManager.getInstance().showLongTosat(getString(R.string.msg_fail_login));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {

        mInterstitialAd.show();

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getString(R.string.alert));
        alertDialog.setMessage(getString(R.string.msg_exit));
        alertDialog.setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
            dialogInterface.dismiss();
            MainActivity.this.finish();
        });
        alertDialog.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss());
        alertDialog.show();
    }

    public void getUserData(String email){
        db.collection(CommonData.COLLECTION_USERS).whereEqualTo(CommonData.FIELD_EMAIL, email).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                UserData sing_user = null;
                for (QueryDocumentSnapshot document : task.getResult()){
                    sing_user = document.toObject(UserData.class).withId(document.getId());
                    CommonData.getInstance().setCurUser(sing_user);
                    CommonData.getInstance().setUserDocId(document.getId());
                }
                if(sing_user != null){
                    ToastManager.getInstance().showLongTosat(email+ " "+ getString(R.string.msg_login_id));

                    db.collection(CommonData.COLLECTION_USERS).document(CommonData.getInstance().getUserDocId()).addSnapshotListener((documentSnapshot, e) -> {
                        CommonData.getInstance().setCurUser(documentSnapshot.toObject(UserData.class).withId(documentSnapshot.getId()));
                        tv_point.setText(String.valueOf(CommonData.getInstance().getCurUser().getPoint()) + " P");
                    });

                    btn_sign.setVisibility(View.GONE);
                }
            }else{
                ToastManager.getInstance().showLongTosat(getString(R.string.msg_not_found_id));
            }
        });
    }

    public void newUserCrate(String email){
        UserData newUser = new UserData(email, 500);
        db.collection(CommonData.COLLECTION_USERS).document().set(newUser).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                getUserData(email);
            }else{
                ToastManager.getInstance().showLongTosat(getString(R.string.msg_fail_create_id));
            }
        });
    }

    @OnClick({R.id.btn_start, R.id.btn_reward,R.id.btn_sign,R.id.btn_help})
    public void OnClick(View view){
        switch (view.getId()){
            case R.id.btn_start:
                if(user != null){
                    if(CommonData.getInstance().isServiceRun()){
                        // 서비스 실행 중 - 종료 행동
                        CommonData.getInstance().setServiceRun(false);
                        btn_start.setText(R.string.start);
                        ToastManager.getInstance().showShortTosat(getString(R.string.end_service));
                        stopService(new Intent(this, TranslationService.class));    //서비스 종료
                    }else{
                        // 서비스 미 실행 중 - 실행 행동
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, REQ_CODE_OVERLAY_PERMISSION);
                        } else {
                            CommonData.getInstance().setServiceRun(true);
                            btn_start.setText(R.string.end);
                            ToastManager.getInstance().showShortTosat(getString(R.string.start_service));
                            startService(new Intent(this, TranslationService.class));    //서비스 시작
                        }
                    }
                }else{
                    ToastManager.getInstance().showShortTosat(getString(R.string.need_login));
                }

                break;
            case R.id.btn_reward:
                if(user != null) {
                    mRewardedVideoAd.show();
                }else{
                    ToastManager.getInstance().showShortTosat(getString(R.string.need_login));
                }
                break;
            case R.id.btn_sign:
                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(), RC_SIGN_IN);
                break;
            case R.id.btn_help:
                if(mHelpDialog == null)
                    mHelpDialog = new HelpDialog(MainActivity.this);

                mHelpDialog.show();
                break;
        }
    }

    @BindView(R.id.btn_sign)    SignInButton btn_sign;
    @BindView(R.id.tv_point)    TextView tv_point;
    @BindView(R.id.btn_reward)  Button btn_reward;
    @BindView(R.id.spinner_source_lang)    Spinner spinner_source_lang;
    @BindView(R.id.spinner_target_lang)     Spinner spinner_target_lang;
    @BindView(R.id.sw_show_original)        Switch sw_show_original;
    @BindView(R.id.ad_view_top)    AdView ad_view_top;
    @BindView(R.id.ad_view_bottom)    AdView ad_view_bottom;

    @BindView(R.id.btn_help)    Button btn_help;

    @BindView(R.id.btn_start)    Button btn_start;
    @BindView(R.id.tv_version)  TextView tv_version;
}
