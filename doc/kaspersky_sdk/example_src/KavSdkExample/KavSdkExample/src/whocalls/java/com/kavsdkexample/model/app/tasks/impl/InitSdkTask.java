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

import com.kaspersky.whocalls.externalapi.WhoCallsSdkFactory;
import com.kaspersky.whocalls.externalapi.WhoCallsSdkInitParamsBuilder;
import com.kavsdk.license.SdkLicense;
import com.kavsdk.license.SdkLicenseException;
import com.kavsdkexample.BuildConfig;
import com.kavsdkexample.core.app.model.interactors.ForegroundCaller;
import com.kavsdkexample.core.app.model.interactors.ServiceInteractor;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.model.app.tasks.InitSdkResultsObserver;
import com.kavsdkexample.model.service.SampleService;

import java.io.File;

public final class InitSdkTask implements Runnable {
    private static final String TAG = InitSdkTask.class.getSimpleName();
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
        Log.d(TAG, "======================= BEGIN =======================");

        /*final KasperskySecurityNetworkSettings ksnSettings = KsnSettingsReader.readSettings(mContext);
        if (ksnSettings != null) {
            KasperskySecurityNetworkConfiguration.setNetworkSettings(ksnSettings);
        }*/

        long startTime = System.currentTimeMillis();
        Log.d(TAG, "perf: initApplication() started");


        Log.d(TAG, "perf: initSafe() started");
        try {
            final File basesPath = mContext.getDir(DEFAULT_BASES_UNPACK_DIR_SUFFIX, Context.MODE_PRIVATE);

            WhoCallsSdkFactory.initializeWhoCallsSdk(
                    mContext,
                    new WhoCallsSdkInitParamsBuilder(basesPath)
                            .setForegroundRequest(() -> {
                                        SampleService.startService(
                                                mContext,
                                                ForegroundCaller.SelfDefense
                                        );
                                        return true;
                                    }
                            )
                            .setUpdaterUrl(BuildConfig.UPDATER_URL.isEmpty() ? null : BuildConfig.UPDATER_URL)
                            .build()
            );

            long initTimeMs = System.currentTimeMillis() - startTime;
            Log.d(TAG, "perf: initSafe() finished in " + initTimeMs + " ms");

            final SdkLicense license = WhoCallsSdkFactory.getLicense();
            if (!license.isClientUserIDRequired()) {
                try {
                    license.activate(null);
                } catch (SdkLicenseException e) {
                    mThreadManager.runOnUiThread(() -> mObserver.onInitException(e));
                    return;
                }
            }
            mThreadManager.runOnUiThread(() -> mObserver.onSdkInited(initTimeMs));
        } catch (Exception e) {
            Log.e(TAG, "Exception during SDK init", e);
            mThreadManager.runOnUiThread(() -> mObserver.onInitException(e));
        }
    }
}
