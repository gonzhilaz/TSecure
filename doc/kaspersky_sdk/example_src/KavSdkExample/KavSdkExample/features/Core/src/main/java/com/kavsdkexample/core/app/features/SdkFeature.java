/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.features;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.kavsdkexample.core.app.SdkStatusProvider;
import com.kavsdkexample.core.app.view.BaseView;

import dagger.android.HasAndroidInjector;

/**
 * This interface hides samples implementation of different SDK functionality from core part of  KavSdkExample.
 * It allows to have KavSdkExample compilable for Mobile SDK with different API supply
 */
@Keep
public interface SdkFeature extends  SdkStatusProvider,
                                     HasAndroidInjector {
    String ANTISPAM                        = "Antispam";
    String ANTIPHISHING                    = "Antiphishing";
    String APP_CATEGORY                    = "AppCategory";
    String ANTIVIRUS_PUA_SCANNER           = "Antivirus-PuaScanner";
    String ANTIVIRUS_PUA_MONITOR           = "Antivirus-PuaMonitor";
    String ANTIVIRUS_EASY_SCANNER          = "Antivirus-EasyScanner";
    String ANTIVIRUS_SECURITY_SCANNER      = "Antivirus-SecurityScanner";
    String ANTIVIRUS_SINGLE_THREAD_SCANNER = "Antivirus-SingleThreadScanner";
    String ANTIVIRUS_MULTI_THREAD_SCANNER  = "Antivirus-MultiThreadScanner";
    String ANTIVIRUS_RTP_MONITOR           = "Antivirus-RtpMonitor";
    String ANTIVIRUS_APP_MONITOR           = "Antivirus-AppMonitor";
    String ANTIVIRUS_FOLDER_MONITOR        = "Antivirus-FolderMonitor";
    String ANTIVIRUS_QUARANTINE            = "Antivirus-Quarantine";
    String ANTIVIRUS_OTHER                 = "Antivirus-Other";
    String ANTIVIRUS_ROOT_CHECKER          = "Antivirus-RootChecker";
    String ANTIVIRUS_SELF_CHECKER          = "Antivirus-SelfChecker";
    String APP_CONTROL                     = "AppControl";
    String SECURE_CONNECTION               = "SecureConnection";
    String SIM_WATCH                       = "SimWatch";
    String DEVICE_CONFIGURATION            = "DeviceConfiguration";
    String DEVICE_FINGERPRINT              = "DeviceFingerprint";
    String SAFE_INPUT                      = "SafeInput";
    String SECURE_STORAGE                  = "SecureStorage";
    String SECURE_SMS                      = "SecureSms";
    String FINGERPRINT_MONITOR             = "FingerprintMonitor";
    String SELF_DEFENSE                    = "SelfDefense";
    String URL_CHECK                       = "UrlCheck";
    String WIFI_SAFETY                     = "WifiSafety";
    String EULA                            = "Eula";
    String UPDATE                          = "Update";
    String STATISTIC                       = "Statistic";
    String AGREEMENTS                      = "Agreements";
    String WHO_CALLS                       = "WhoCalls";
    String SIMPLE_URL_REPUTATION           = "SimpleUrlReputation";
    String INTERNAL                        = "Internal";
    String DISCOVERY                       = "Discovery";
    String PRIVACY_COMPROMISED_ACCOUNTS    = "Compromised Accounts";
    String PRIVACY_COMPROMISED_PASSWORDS   = "Compromised Passwords";

    /**
     * Unique name of SDK component
     * @return SDK component name that unique for each component
     */
    @NonNull
    String getName();

    @UiThread
    boolean onSdkInited();

    @UiThread
    void onOwnerViewEvent(OwnerViewEvent event, BaseView view);

    @UiThread
    TabInfo provideFeatureTab(@Nullable TabCompletionCallback callback);

    interface TabCompletionCallback {
        void onTabResult(@NonNull TabResult result);
    }

    enum TabResult {
        Accept,
        Reject
    }

    enum OwnerViewEvent {
        ViewCreate,
        ViewDestroy
    }
}
