/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.self_defense.model.sdk.impl;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdk.KavSdk;
import com.kavsdk.notificationsaccess.NotificationAccessListener;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.self_defense.model.sdk.NotificationStateObserver;
import com.kavsdkexample.self_defense.model.sdk.SdkIntentProvider;
import com.kavsdkexample.self_defense.model.sdk.SdkManager;

import java.util.concurrent.ExecutorService;

public final class SdkManagerImpl implements SdkManager,
                                             SdkIntentProvider,
                                             NotificationAccessListener {
    @NonNull private final Context         mContext;
    @NonNull private final ExecutorService mExecutor;
    @NonNull private final ThreadManager   mThreadManager;

    @Nullable
    private NotificationStateObserver mObserver;

    public SdkManagerImpl(@NonNull Context         context,
                          @NonNull ExecutorService executorService,
                          @NonNull ThreadManager   threadManager) {
        mContext       = context;
        mExecutor      = executorService;
        mThreadManager = threadManager;
    }

    @Override
    public void enableAutoRestart(boolean value) {
        KavSdk.setSdkAutoRestartEnabled(value);
    }

    @Override
    public boolean isAutoRestartEnabled() {
        return KavSdk.isSdkAutoRestartEnabled();
    }

    @Override
    public boolean isNotificationAccessEnabled() {
        return KavSdk.getNotificationAccess().isEnabled();
    }

    @Override
    public void setNotificationAccessStateObserver(@Nullable NotificationStateObserver observer) {
        KavSdk.getNotificationAccess().setListener(observer == null ? null : this);
        mObserver = observer;
    }

    @Override
    public void checkAppSignature(@NonNull String certFileName, @NonNull AppSignatureCheckObserver observer) {
        mExecutor.execute(new CheckAppSignatureTask(mContext, mThreadManager, certFileName, observer));
    }

    @Override
    public Intent getNotificationAccessIntent() {
        return KavSdk.getNotificationAccess().getGrantingIntent();
    }

    @Override
    public void onNotificationAccessChanged(boolean isEnabled) {
        if (mObserver != null) {
            mObserver.onNotificationAccessStateChanged(isEnabled);
        }
    }
}
