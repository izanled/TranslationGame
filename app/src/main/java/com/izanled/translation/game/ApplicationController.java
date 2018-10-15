package com.izanled.translation.game;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.izanled.translation.game.common.CommonData;
import com.izanled.translation.game.utils.ToastManager;

public class ApplicationController extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        FirebaseAnalytics.getInstance(this);
        CommonData.getInstance(this);
        ToastManager.getInstance(this);
        MobileAds.initialize(this, "ca-app-pub-8427929259216639~8934031257");
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);
    }
}
