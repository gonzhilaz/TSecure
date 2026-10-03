/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.app.tasks.impl;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import android.util.Log;

import com.kaspersky.perftools.PerformanceConfigurator;
import com.kavsdk.KavSdk;
import com.kavsdk.license.SdkLicense;
import com.kavsdk.license.SdkLicenseClientIdException;
import com.kavsdk.license.SdkLicenseException;
import com.kavsdk.network.KasperskySecurityNetworkConfiguration;
import com.kavsdk.network.KasperskySecurityNetworkSettings;
import com.kavsdkexample.BuildConfig;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.model.app.sdk.impl.KsnSettingsReader;
import com.kavsdkexample.model.app.tasks.InitSdkResultsObserver;
import com.kavsdkexample.core.app.model.interactors.ForegroundCaller;
import com.kavsdkexample.core.app.model.interactors.ServiceInteractor;
import com.kavsdkexample.view.sdk.impl.ProxyAuthActivityLauncher;

import java.io.File;
import java.io.IOException;

public final class InitSdkTask implements Runnable {
    private static final String TAG                             = InitSdkTask.class.getSimpleName();
    private static final String DEFAULT_BASES_UNPACK_DIR_SUFFIX = "bases";
    private final Context                mContext;
    private final ThreadManager          mThreadManager;
    private final ServiceInteractor      mServiceInteractor;
    private final InitSdkResultsObserver mObserver;

    InitSdkTask(@NonNull Context                context,
                @NonNull ThreadManager          threadManager,
                @NonNull ServiceInteractor      serviceInteractor,
                @NonNull InitSdkResultsObserver observer) {
        mContext            = context;
        mThreadManager      = threadManager;
        mServiceInteractor  = serviceInteractor;
        mObserver           = observer;
    }

    private String getNativeLibsPath() {

        // If you do not want to store native libraries in the application data directory
        // SDK provides the ability to specify another path (otherwise, you can simply omit pathToLibraries parameter).
        // Note: storing the libraries outside the application data directory is not secure as
        // the libraries can be replaced. In this case, the libraries correctness checking is required.
        // Besides, the specified path must be to device specific libraries, i.e. you should care about device architecture.
        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            return packageInfo.applicationInfo.nativeLibraryDir;

        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @WorkerThread
    public void run() {
        if (BuildConfig.DEBUG) {
            mThreadManager.checkWorkerThread();
        }
        PerformanceConfigurator.enableProfiler(PerformanceConfigurator.PROFILE_INIT_APPLICATION);
        Log.d(TAG, "======================= BEGIN =======================");

        KavSdk.setForegroundRequest(() -> mServiceInteractor.startService(ForegroundCaller.SelfDefense));

        final KasperskySecurityNetworkSettings ksnSettings = KsnSettingsReader.readSettings(mContext);
        if (ksnSettings != null) {
            KasperskySecurityNetworkConfiguration.setNetworkSettings(ksnSettings);
        }

        long startTime = System.currentTimeMillis(); // NOPMD
        Log.d(TAG, "perf: initApplication() started");

        final File basesPath = mContext.getDir(DEFAULT_BASES_UNPACK_DIR_SUFFIX, Context.MODE_PRIVATE);
        KavSdk.setProxyAuthListener(new ProxyAuthActivityLauncher(mContext));

        Log.d(TAG, "perf: initSafe() started");
        try {
            // Don't delete the getSdkName() call - for kashell
            Log.i(TAG, "Starting SDK: " + KavSdk.getSdkName());
            // Don't delete the registerProfiler() call - for kashell
            PerformanceConfigurator.registerProfiler(PerformanceConfigurator.KEEP_ALIVE_ANTI_PROGUARD);

            KavSdk.initSafe(mContext, basesPath, getNativeLibsPath());
            // Don't delete the getPathToBases call - for kashell
            Log.d(TAG, "perf: path to bases: " + KavSdk.getPathToBases());
        } catch (IOException e) {
            mThreadManager.runOnUiThread(() -> mObserver.onInitException(e));
            return;
        }
        long initTimeMs = System.currentTimeMillis() - startTime;
        Log.d(TAG, "perf: initSafe() finished in " + initTimeMs + " ms");

        final SdkLicense license = KavSdk.getLicense();
        if (!license.isValid() && license.isClientUserIDRequired()) {
            handleException(new SdkLicenseClientIdException());
            return;
        }
        if (!license.isClientUserIDRequired()) {
            try {
                license.activate(null);
            } catch (final SdkLicenseException e) {
                handleException(e);
                return;
            }
        }
        PerformanceConfigurator.disableProfiler(PerformanceConfigurator.PROFILE_INIT_APPLICATION);
        mThreadManager.runOnUiThread(() -> mObserver.onSdkInited(initTimeMs));
    }

    private void handleException(final SdkLicenseException e) {
        // Don't delete the getErrorCode call - it's for kashell
        Log.e(TAG, String.format("SdkLicence error: %d, msg=%s", e.getErrorCode(), e.getMessage()));
        mThreadManager.runOnUiThread(() -> mObserver.onInitException(e));
    }
}
