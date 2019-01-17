package com.izanled.translation.game;


import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.izanled.translation.game.common.CommonData;
import com.izanled.translation.game.utils.ToastManager;

import io.fabric.sdk.android.Fabric;

public class ApplicationController extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        CommonData.getInstance(this);
        ToastManager.getInstance(this);
        MobileAds.initialize(this, "ca-app-pub-8427929259216639~8934031257");
        Fabric.with(this, new Crashlytics());

        FirebaseApp.initializeApp(this);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        FirebaseAnalytics.getInstance(this);

    }
}
