/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.self_defense.model.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.kavsdkexample.core.app.model.impl.BaseModelImpl;
import com.kavsdkexample.core.app.model.interactors.ForegroundCaller;
import com.kavsdkexample.core.app.model.interactors.NotificationInteractor;
import com.kavsdkexample.core.app.model.interactors.ServiceInteractor;
import com.kavsdkexample.self_defense.model.ManageableSelfDefenseModel;
import com.kavsdkexample.self_defense.model.SelfDefenseModelAppSignCheckObserver;
import com.kavsdkexample.self_defense.model.SelfDefenseModelObserver;
import com.kavsdkexample.self_defense.model.sdk.NotificationStateObserver;
import com.kavsdkexample.self_defense.model.sdk.SdkManager;
import com.kavsdkexample.self_defense.model.settings.Settings;

@UiThread
public class SelfDefenseModelImpl extends    BaseModelImpl
                                  implements ManageableSelfDefenseModel,
                                             SdkManager.AppSignatureCheckObserver {

    /**
    * A file name of a debug certificate, which is located in assets.
    * Note: For a 'real' application the certificate should be located in a safe place.
    * It will be a good choice to retrieve the certificate from a server at runtime.
    */
    private static final String CERTIFICATE_FILE_NAME = "android_debug.crt";

    private final SdkManager             mSdkManager;
    private final Settings               mSettings;
    private       ServiceInteractor      mServiceInteractor;


    public SelfDefenseModelImpl(@NonNull SdkManager       sdkManager,
                                @NonNull Settings         settings) {
        mSdkManager      = sdkManager;
        mSettings        = settings;
    }

    @Override
    public void onSdkInited() {
        super.onSdkInited();
        if (mSettings.isForegroundServiceEnabled()) {
            mServiceInteractor.startService(ForegroundCaller.SelfDefense);
        }
    }

    @Override
    public void setSdkAutoRestartEnabled(boolean value) {
        mSdkManager.enableAutoRestart(value);
    }

    @Override
    public boolean isSdkAutorestartEnabled() {
        return mSdkManager.isAutoRestartEnabled();
    }

    @Override
    public void enableForegroundService() {
        mSettings.setForegroundServiceEnabled(true);
        mServiceInteractor.startService(ForegroundCaller.SelfDefense);
    }

    @Override
    public void disableForegroundService() {
        mServiceInteractor.stopService(ForegroundCaller.SelfDefense);
        mSettings.setForegroundServiceEnabled(false);
    }

    @Override
    public boolean isForegroundServiceEnabled() {
        return mSettings.isForegroundServiceEnabled();
    }

    @Override
    public boolean isNotificationAccessEnabled() {
        return mSdkManager.isNotificationAccessEnabled();
    }

    @Override
    public void setNotificationStateObserver(@Nullable NotificationStateObserver observer) {
        mSdkManager.setNotificationAccessStateObserver(observer);
    }

    @Override
    public void switchToCheckSignatureView() {
        notifyObservers(
                observer -> observer.onChangeViewRequest(SelfDefenseModelObserver.ViewType.CheckSignatureFragment),
                SelfDefenseModelObserver.class);
    }

    @Override
    public void switchToSelfDefenseFeatureView() {
        notifyObservers(
                observer -> observer.onChangeViewRequest(SelfDefenseModelObserver.ViewType.SelfDefenseFeatureFragment),
                SelfDefenseModelObserver.class);
    }

    @Override
    public void checkApplicationSignature() {
        mSdkManager.checkAppSignature(CERTIFICATE_FILE_NAME, this);
    }

    @Override
    public void setInteractors(@NonNull ServiceInteractor serviceInteractor, @NonNull NotificationInteractor notificationInteractor) {
        mServiceInteractor      = serviceInteractor;
    }

    @Override
    public void onAppSignatureCheckResult(@NonNull String result) {
        notifyObservers(observer -> observer.onApplicationCheckResult(result), SelfDefenseModelAppSignCheckObserver.class);
    }
}
