/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.pua_monitor.sdk.impl;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.kavsdk.antivirus.AntivirusInstance;
import com.kavsdk.antivirus.ThreatInfoSerializer;
import com.kavsdk.antivirus.puaprotection.PuaCloudConnectionFailedListener;
import com.kavsdk.antivirus.puaprotection.PuaInfo;
import com.kavsdk.antivirus.puaprotection.puamonitor.PuaInstallationMonitorListener;
import com.kavsdk.antivirus.puaprotection.puamonitor.PuaInstallationMonitor;

import com.kavsdkexample.antivirus.base.model.ThreatInfoWrapper;
import com.kavsdkexample.antivirus.base.model.ThreatInfoWrapperFactory;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatFoundedBy;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatInfoCollector;
import com.kavsdkexample.antivirus.base.view.UiLauncher;
import com.kavsdkexample.antivirus.pua_monitor.sdk.PuaMonitorManager;
import com.kavsdkexample.core.app.utils.ThreadManager;

import java.util.concurrent.ExecutorService;

public class PuaMonitorManagerImpl implements PuaMonitorManager, PuaInstallationMonitorListener, PuaCloudConnectionFailedListener {
    private final static String TAG = PuaMonitorManagerImpl.class.getSimpleName();

    private final Context mContext;
    private final ThreadManager mThreadManager;
    private final ExecutorService mExecutorService;
    private final ThreatInfoWrapperFactory mThreatInfoWrapperFactory;
    private final ThreatInfoCollector mThreatInfoCollector;
    private final UiLauncher mUiLauncher;
    private PuaInstallationMonitor mPuaInstallationMonitor;

    public PuaMonitorManagerImpl(@NonNull Context                  context,
                                 @NonNull ThreadManager            threadManager,
                                 @NonNull ExecutorService          executorService,
                                 @NonNull ThreatInfoWrapperFactory threatInfoWrapperFactory,
                                 @NonNull ThreatInfoCollector      threatInfoCollector,
                                 @NonNull UiLauncher               uiLauncher) {
        mContext                  = context;
        mThreadManager            = threadManager;
        mExecutorService          = executorService;
        mThreatInfoWrapperFactory = threatInfoWrapperFactory;
        mThreatInfoCollector      = threatInfoCollector;
        mUiLauncher               = uiLauncher;
    }

    @Override
    public void onPuaInstallationDetect(PuaInfo puaInfo) {
        String packageName = puaInfo.getPackageName();
        String category = puaInfo.getPuaCategory().toString();

        Log.d(TAG, "PuaInstallationMonitor: detected " + packageName + " has category " + category);

        ThreatInfoWrapper wrappedThreat = mThreatInfoWrapperFactory.createThreatInfo(
                category,
                puaInfo.getFileFullPath(),
                puaInfo.getObjectName(),
                packageName == null ? "" : packageName,
                ThreatInfoSerializer.toBytes(puaInfo),
                puaInfo.getSeverityLevel().getCode(),
                false
        );
        mThreatInfoCollector.addThreat(wrappedThreat, ThreatFoundedBy.Oas);
        mUiLauncher.launchDetectedApplicationsView();
    }

    @Override
    public void onCloudConnectionFailed(PuaInfo puaInfo) {
        mThreadManager.runOnUiThread(
            () -> Toast.makeText(mContext,
                                 String.format("Can't connect to KSN cloud, app '%s' was not scanned",
                                               puaInfo.getPackageName()),
                                 Toast.LENGTH_LONG).show()
        );
    }

    @Override
    public void init() {
        mPuaInstallationMonitor = AntivirusInstance.getInstance().createPuaInstallationMonitor();
    }

    @Override
    public void applyMonitorState(boolean enabled, @NonNull MonitorStateObserver observer) {
        mExecutorService.execute(() -> {
            if (enabled) {
                mPuaInstallationMonitor.enable(this, this);
            } else {
                mPuaInstallationMonitor.disable();
            }
            mThreadManager.runOnUiThread(() -> observer.onMonitorState(mPuaInstallationMonitor.isEnabled()));
        });
    }

    @Override
    public boolean getMonitorState() {
        return mPuaInstallationMonitor.isEnabled();
    }

    @Override
    public void applyCloudOnlyCheck(boolean enabled) {}

    @Override
    public void applyCloudCheck(boolean enabled) {}

    @Override
    public void applyCheckRiskware(boolean enabled) {}

    @Override
    public void applySuspiciousCheck(boolean enabled) {}

    @Override
    public void applyMaxFileCheckSize(long size) {}

    @Override
    public void applyProcessMissedApps(boolean enabled) {}
}
