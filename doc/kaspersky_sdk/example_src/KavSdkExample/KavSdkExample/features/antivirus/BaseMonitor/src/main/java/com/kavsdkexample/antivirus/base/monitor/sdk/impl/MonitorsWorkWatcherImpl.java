/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.monitor.sdk.impl;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;

import com.kavsdk.antivirus.AntivirusInstance;
import com.kavsdk.antivirus.MonitoringListener;
import com.kavsdkexample.antivirus.base.monitor.sdk.MonitorsWorkWatcher;
import com.kavsdkexample.core.app.model.interactors.ForegroundCaller;
import com.kavsdkexample.core.app.model.interactors.ServiceInteractor;
import com.kavsdkexample.core.app.utils.ThreadManager;

public class MonitorsWorkWatcherImpl implements MonitorsWorkWatcher,
                                                MonitoringListener {
    private static final int FLAG_RTP_MONITOR_ENABLED = 0x0001;
    private static final int FLAG_APP_MONITOR_ENABLED = 0x0002;
    private static final int FLAG_FLD_MONITOR_ENABLED = 0x0004;

    private static MonitorsWorkWatcherImpl sInstance;
    private final ThreadManager     mThreadManager;
    private final ServiceInteractor mServiceInteractor;

    private int     mMonitorEnabledFlags;
    private boolean mIsForeground;

    @NonNull
    @UiThread
    public static MonitorsWorkWatcher getInstance(@NonNull ThreadManager     threadManager,
                                                  @NonNull ServiceInteractor serviceInteractor) {
        if (sInstance == null) {
            sInstance = new MonitorsWorkWatcherImpl(threadManager, serviceInteractor);
        }
        return sInstance;
    }

    private MonitorsWorkWatcherImpl(@NonNull ThreadManager     threadManager,
                                    @NonNull ServiceInteractor serviceInteractor) {
        mThreadManager     = threadManager;
        mServiceInteractor = serviceInteractor;
        AntivirusInstance.getInstance().setMonitoringListener(this);
    }

    private static int getMonitorFlag(int monitorId) {
        switch (monitorId) {
            case RTP_MONITOR_ID:
                return FLAG_RTP_MONITOR_ENABLED;
            case APPLICATION_MONITOR_ID:
                return FLAG_APP_MONITOR_ENABLED;
            case FOLDER_MONITOR_ID:
                return FLAG_FLD_MONITOR_ENABLED;
            default:
                throw new IllegalStateException("Unknown monitor id: " + monitorId);
        }

    }

    @Override
    @WorkerThread
    public void onMonitorStart(int monitorId) {
        mThreadManager.runOnUiThread(() -> {
            mMonitorEnabledFlags |= getMonitorFlag(monitorId);
            updateForeground();
        });
    }

    @Override
    @WorkerThread
    public void onMonitorStop(int monitorId) {
        mThreadManager.runOnUiThread(() -> {
            mMonitorEnabledFlags &= ~getMonitorFlag(monitorId);
            updateForeground();
        });
    }

    @UiThread
    private void updateForeground() {
        boolean foreground = mMonitorEnabledFlags != 0;
        if (mIsForeground != foreground) {
            if (foreground) {
                mServiceInteractor.startService(ForegroundCaller.Antivirus);
            } else {
                mServiceInteractor.stopService(ForegroundCaller.Antivirus);
            }
            mIsForeground = foreground;
        }
    }
}
