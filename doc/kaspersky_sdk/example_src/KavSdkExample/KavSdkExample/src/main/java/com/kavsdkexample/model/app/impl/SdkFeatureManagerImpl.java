/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.app.impl;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import android.util.Log;

import com.kavsdkexample.BuildConfig;
import com.kavsdkexample.core.app.ExtendableInjector;
import com.kavsdkexample.core.app.features.SdkFeature;
import com.kavsdkexample.core.app.model.interactors.NotificationInteractor;
import com.kavsdkexample.core.app.model.interactors.ServiceInteractor;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.model.SdkFeatureManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dagger.android.AndroidInjector;

public final class SdkFeatureManagerImpl implements SdkFeatureManager {
    private static final String TAG = SdkFeatureManagerImpl.class.getSimpleName();
    private static final List<String> FEATURE_CLASSES = new ArrayList<>();
    private static final String[] ALWAYS_ENABLED_CLASSES = {
        "com.kavsdkexample.eula.EulaSdkFeature",
        "com.kavsdkexample.antivirus.app_monitor.AppMonitorSdkFeature",
        "com.kavsdkexample.antivirus.easy_scanner.EasyScannerSdkFeature",
        "com.kavsdkexample.antivirus.security_scanner.SecurityScannerSdkFeature",
        "com.kavsdkexample.antivirus.folder_monitor.FolderMonitorSdkFeature",
        "com.kavsdkexample.antivirus.other.AvOtherSdkFeature",
        "com.kavsdkexample.antivirus.quarantine.QuarantineSdkFeature",
        "com.kavsdkexample.antivirus.rtp_monitor.RtpMonitorSdkFeature",
        "com.kavsdkexample.antivirus.single_thread_scanner.SingleThreadScannerSdkFeature",
        "com.kavsdkexample.antivirus.multi_thread_scanner.MultiThreadScannerSdkFeature",
        "com.kavsdkexample.antivirus.pua_scanner.PuaScannerSdkFeature",
        "com.kavsdkexample.antivirus.pua_monitor.PuaMonitorSdkFeature",
        "com.kavsdkexample.antivirus.root_checker.RootCheckerSdkFeature",
        "com.kavsdkexample.antivirus.self_checker.SelfCheckerSdkFeature",
        "com.kavsdkexample.appcategory.AppCategorySdkFeature",
        "com.kavsdkexample.appcontrol.AppControlSdkFeature",
        "com.kavsdkexample.antiphishing.AntiPhishingSdkFeature",
        "com.kavsdkexample.device_configuration.DeviceConfigurationSdkFeature",
        "com.kavsdkexample.device_fingerprint.DeviceFingerprintSdkFeature",
        "com.kavsdkexample.safe_input.SafeInputSdkFeature",
        "com.kavsdkexample.secure_connectivity.SecureConnectivitySdkFeature",
        "com.kavsdkexample.secure_sms.SecureSmsSdkFeature",
        "com.kavsdkexample.secure_storage.SecureStorageSdkFeature",
        "com.kavsdkexample.self_defense.SelfDefenseSdkFeature",
        "com.kavsdkexample.sim_watch.SimWatchSdkFeature",
        "com.kavsdkexample.url_check.UrlCheckSdkFeature",
        "com.kavsdkexample.wifi_safety.WifiSafetySdkFeature",
        "com.kavsdkexample.statistic.StatisticSdkFeature",
        "com.kavsdkexample.update.UpdateSdkFeature",
        "com.kavsdkexample.agreements.AgreementsSdkFeature",
        "com.kavsdkexample.antispam.AntiSpamSdkFeature",
        "com.kavsdkexample.fingerprintmonitor.FingerprintMonitorSdkFeature",
        "com.kavsdkexample.whocalls.WhoCallsSdkFeature",
        "com.kavsdkexample.simpleurlreputation.SimpleUrlReputationSdkFeature",
        "com.kavsdkexample.internal.InternalSdkFeature"
    };
    private final ThreadManager           mThreadManager;
    private final Map<String, SdkFeature> mFeaturesMap;

    private void addIfEnabled(boolean isFeatureEnabled, final String clazzName) {
        try {
            if (isFeatureEnabled) {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "Enabled feature: " + clazzName);
                }
                FEATURE_CLASSES.add(clazzName);
            }
        } catch (UnsupportedOperationException uoe) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Fatal: " + uoe.getMessage());
            }
        }
    }

    @UiThread
    public SdkFeatureManagerImpl(@NonNull Application            application,
                                 @NonNull ThreadManager          threadManager,
                                 @NonNull ServiceInteractor      serviceInteractor,
                                 @NonNull NotificationInteractor notificationInteractor) {
        mThreadManager = threadManager;
        mFeaturesMap   = new HashMap<>();

        if (BuildConfig.DEBUG) {
            threadManager.checkUiThread();
        }

        for (String clazzName : ALWAYS_ENABLED_CLASSES) {
            FEATURE_CLASSES.add(clazzName);
        }
        addIfEnabled(BuildConfig.COMPROMISED_ACCOUNTS_ENABLED, "com.kavsdkexample.privacy.compromised_accounts.CompromisedAccountsSdkFeature");
        addIfEnabled(BuildConfig.COMPROMISED_PASSWORDS_ENABLED, "com.kavsdkexample.privacy.compromised_passwords.CompromisedPasswordsSdkFeature");

        for (String featureClass : FEATURE_CLASSES) {
            Class<?> cls;
            try {
                cls = Class.forName(featureClass);
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "Feature for class [" + featureClass + "] not found]");
                }
                continue;
            }
            SdkFeature feature;
            try {
                feature = (SdkFeature) cls.getConstructor(Application.class,
                                                          ServiceInteractor.class,
                                                          NotificationInteractor.class)
                                          .newInstance(application,
                                                       serviceInteractor,
                                                       notificationInteractor);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create SDK feature from class: " + featureClass, e);
            }
            mFeaturesMap.put(feature.getName(), feature);
        }
    }


    @Override
    @Nullable
    public <T extends SdkFeature>T findSdkFeature(@NonNull String name) {
        //noinspection unchecked
        return (T) mFeaturesMap.get(name);
    }

    @NonNull
    @Override
    public Collection<SdkFeature> allFeatures() {
        return Collections.unmodifiableCollection(mFeaturesMap.values());
    }

    @Override
    @UiThread
    public void onSdkInited() {
        if (BuildConfig.DEBUG) {
            mThreadManager.checkUiThread();
        }
        Collection<SdkFeature> features = mFeaturesMap.values();
        for (SdkFeature feature : features) {
            feature.onSdkInited();
        }
    }

    @Override
    public void onSdkInitFailed() {
    }

    @Override
    public void combineFeaturesInjectors(@NonNull ExtendableInjector injector) {
        Collection<SdkFeature> features = mFeaturesMap.values();
        for (SdkFeature feature : features) {
            AndroidInjector<Object> featureInjector = feature.androidInjector();
            if (featureInjector instanceof ExtendableInjector) {
                ExtendableInjector<Object> exInjector = (ExtendableInjector<Object>)featureInjector;
                injector.combineWith(exInjector);
            } else if (featureInjector != null) { // Skip features without injectors
                throw new IllegalStateException("Extendable injector for is expected here");
            }
        }
    }
}
