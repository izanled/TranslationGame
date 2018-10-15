package com.izanled.translation.game.view;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.izanled.translation.game.R;
import com.izanled.translation.game.common.CommonData;
import com.izanled.translation.game.data.UserData;
import com.izanled.translation.game.eventbus.BitmapData;
import com.izanled.translation.game.service.TranslationService;
import com.izanled.translation.game.utils.ToastManager;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements RewardedVideoAdListener {
    private static final String TAG = MainActivity.class.getName();

    private static final int REQUEST_CODE = 100;
    private static String STORE_DIRECTORY;
    private static final String SCREENCAP_NAME = "screencap";
    private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    private static MediaProjection sMediaProjection;

    public static final int REQ_CODE_OVERLAY_PERMISSION = 9999;

    private MediaProjectionManager mProjectionManager;
    private ImageReader mImageReader;
    private Handler mHandler;
    private Display mDisplay;
    private VirtualDisplay mVirtualDisplay;
    private int mDensity;
    private int mWidth;
    private int mHeight;
    private int mRotation;
    private OrientationChangeCallback mOrientationChangeCallback;

    private InterstitialAd mInterstitialAd;
    private RewardedVideoAd mRewardedVideoAd;
    private FirebaseFirestore db;

    private int point = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initLayout();
        initAdMob();
        db = FirebaseFirestore.getInstance();
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        db.collection(CommonData.COLLECTION_USERS).document(CommonData.getInstance().getCurUser().get_id()).addSnapshotListener((documentSnapshot, e) -> {
            CommonData.getInstance().setCurUser(documentSnapshot.toObject(UserData.class));
            CommonData.getInstance().getCurUser().set_id(documentSnapshot.getId());
            tv_point.setText(String.valueOf(CommonData.getInstance().getCurUser().getPoint()) + " P");
        });

        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mHandler = new Handler();
                Looper.loop();
            }
        }.start();
    }

    private void initLayout(){
        spinner_source_lang.setSelection(CommonData.getInstance().getSourceLang());
        spinner_target_lang.setSelection(CommonData.getInstance().getTargetLang());
        sw_orientation.setOnCheckedChangeListener((compoundButton, b) -> {
            CommonData.getInstance().setOrientation(b);
            if(b){
                sw_orientation.setText(getString(R.string.landscape));
                MainActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }else{
                sw_orientation.setText(getString(R.string.portrait));
                MainActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        });

        sw_show_original.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b)
                sw_show_original.setText(R.string.show);
            else
                sw_show_original.setText(R.string.hide);
            CommonData.getInstance().setIsShowOriginal(b);
        });

        sw_orientation.setChecked(CommonData.getInstance().getOrientation());
        sw_show_original.setChecked(CommonData.getInstance().getIsShowOriginal());

        if(CommonData.getInstance().getOrientation())
            sw_orientation.setText(getString(R.string.landscape));
        else
            sw_orientation.setText(getString(R.string.portrait));

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
        ad_view.loadAd(adRequest);

        // 전면 광고 초기화
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
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
        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917",
                new AdRequest.Builder().build());
    }

    /**
     * 리워드 광고 리스너
     */
    @Override
    public void onRewarded(RewardItem reward) {
        Toast.makeText(this, "onRewarded! currency: " + reward.getType() + "  amount: " +
                reward.getAmount(), Toast.LENGTH_SHORT).show();
        // Reward the user.
        point = point + 100;
        tv_point.setText(String.valueOf(point));
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        Toast.makeText(this, "onRewardedVideoAdLeftApplication",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdClosed() {
        Toast.makeText(this, "onRewardedVideoAdClosed", Toast.LENGTH_SHORT).show();
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
        Toast.makeText(this, "onRewardedVideoAdFailedToLoad", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        Toast.makeText(this, "onRewardedVideoAdLoaded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdOpened() {
        Toast.makeText(this, "onRewardedVideoAdOpened", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoStarted() {
        Toast.makeText(this, "onRewardedVideoStarted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoCompleted() {
        Toast.makeText(this, "onRewardedVideoCompleted", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ad_view.postDelayed(() -> mInterstitialAd.show(), 1000l);
    }

    @Override
    protected void onPause() {
        mRewardedVideoAd.pause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        mRewardedVideoAd.resume(this);
        if(isServiceRunningCheck())
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
        stopProjection();
    }


    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = null;
            FileOutputStream fos = null;
            Bitmap bitmap = null;

            try {
                image = reader.acquireLatestImage();
                if(CommonData.getInstance().isStarted()){
                    CommonData.getInstance().setStarted(false);
                    if (image != null) {
                        Image.Plane[] planes = image.getPlanes();
                        ByteBuffer buffer = planes[0].getBuffer();
                        int pixelStride = planes[0].getPixelStride();
                        int rowStride = planes[0].getRowStride();
                        int rowPadding = rowStride - pixelStride * mWidth;

                        DisplayMetrics matrix = new DisplayMetrics();
                        ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getMetrics(matrix);		//화면 정보를 가져와서

                        // create bitmap
                        bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888);
                        bitmap.copyPixelsFromBuffer(buffer);

                        Bitmap bitmap1 = bitmap.copy(bitmap.getConfig(), true);
                        BitmapData bitmapData = new BitmapData(bitmap1);
                        EventBus.getDefault().post(bitmapData);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }

                if (bitmap != null) {
                    bitmap.recycle();
                }

                if (image != null) {
                    image.close();
                }
            }
        }
    }

    private class OrientationChangeCallback extends OrientationEventListener {

        OrientationChangeCallback(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            final int rotation = mDisplay.getRotation();
            if (rotation != mRotation) {
                mRotation = rotation;
                try {
                    // clean up
                    if (mVirtualDisplay != null) mVirtualDisplay.release();
                    if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);

                    // re-create virtual display depending on device width / height
                    createVirtualDisplay();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            Log.e("ScreenCapture", "stopping projection.");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mVirtualDisplay != null) mVirtualDisplay.release();
                    if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);
                    if (mOrientationChangeCallback != null) mOrientationChangeCallback.disable();
                    sMediaProjection.unregisterCallback(MediaProjectionStopCallback.this);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            sMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);

            if (sMediaProjection != null) {
                File externalFilesDir = getExternalFilesDir(null);
                if (externalFilesDir != null) {
                    STORE_DIRECTORY = externalFilesDir.getAbsolutePath() + "/screenshots/";
                    File storeDirectory = new File(STORE_DIRECTORY);
                    if (!storeDirectory.exists()) {
                        boolean success = storeDirectory.mkdirs();
                        if (!success) {
                            Log.e(TAG, "failed to create file storage directory.");
                            return;
                        }
                    }
                } else {
                    Log.e(TAG, "failed to create file storage directory, getExternalFilesDir is null.");
                    return;
                }

                // display metrics
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                mDensity = metrics.densityDpi;
                mDisplay = getWindowManager().getDefaultDisplay();

                // create virtual display depending on device width / height
                //if(CommonData.getInstance().isStarted())
                createVirtualDisplay();

                // register orientation change callback
                mOrientationChangeCallback = new OrientationChangeCallback(this);
                if (mOrientationChangeCallback.canDetectOrientation()) {
                    mOrientationChangeCallback.enable();
                }

                // register media projection stop callback
                sMediaProjection.registerCallback(new MediaProjectionStopCallback(), mHandler);
            }
        }
    }

    /****************************************** UI Widget Callbacks *******************************/
    public void startProjection() {
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }

    private void stopProjection() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (sMediaProjection != null) {
                    sMediaProjection.stop();
                }
            }
        });
    }

    /****************************************** Factoring Virtual Display creation ****************/
    private void createVirtualDisplay() {
        // get width and height
        Point size = new Point();
        mDisplay.getSize(size);
        mWidth = size.x;
        mHeight = size.y;

        // start capture reader
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
        mVirtualDisplay = sMediaProjection.createVirtualDisplay(SCREENCAP_NAME, mWidth, mHeight, mDensity, VIRTUAL_DISPLAY_FLAGS, mImageReader.getSurface(), null, mHandler);
        mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), mHandler);
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

    /**
     * 서비스 유무 체크
     * @return
     */
    public boolean isServiceRunningCheck() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
            if ("com.izanled.translation.game.service.TranslationService".equals(service.service.getClassName()))
                return true;

        return false;
    }


    @OnClick({R.id.btn_start, R.id.btn_reward})
    public void OnClick(View view){
        switch (view.getId()){
            case R.id.btn_start:
                if(isServiceRunningCheck()){
                    // 서비스 실행 중 - 종료 행동
                    btn_start.setText(R.string.end);
                    ToastManager.getInstance().showShortTosat(getString(R.string.end_service));
                    stopService(new Intent(this, TranslationService.class));    //서비스 종료
                    mInterstitialAd.show();
                }else{
                    // 서비스 미 실행 중 - 실행 행동
                    btn_start.setText(R.string.start);
                    ToastManager.getInstance().showShortTosat(getString(R.string.start_service));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, REQ_CODE_OVERLAY_PERMISSION);
                    } else {
                        startService(new Intent(this, TranslationService.class));    //서비스 시작
                        startProjection();
                    }
                }

                break;
            case R.id.btn_reward:
                mRewardedVideoAd.show();
                break;
        }
    }

    @BindView(R.id.tv_point)    TextView tv_point;
    @BindView(R.id.btn_reward)  Button btn_reward;
    @BindView(R.id.spinner_source_lang)    Spinner spinner_source_lang;
    @BindView(R.id.spinner_target_lang)     Spinner spinner_target_lang;
    @BindView(R.id.sw_orientation)          Switch sw_orientation;
    @BindView(R.id.sw_show_original)        Switch sw_show_original;
    @BindView(R.id.ad_view)    AdView ad_view;

    @BindView(R.id.btn_start)    Button btn_start;
}
