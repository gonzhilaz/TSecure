/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.folder_monitor.sdk.impl;

import android.content.Context;
import android.content.UriPermission;
import android.os.Build;
import androidx.annotation.NonNull;
import android.util.Log;

import com.kavsdk.antivirus.SuspiciousThreatType;
import com.kavsdk.antivirus.ThreatInfo;
import com.kavsdk.antivirus.ThreatType;
import com.kavsdk.antivirus.foldermonitor.FolderMonitor;
import com.kavsdk.antivirus.foldermonitor.FolderMonitorBuilder;
import com.kavsdk.antivirus.foldermonitor.FolderMonitorListenerV2;
import com.kavsdk.antivirus.foldermonitor.FolderMonitorSuspiciousListener;
import com.kavsdk.license.SdkLicenseViolationException;
import com.kavsdkexample.antivirus.base.monitor.model.MonitorBaseManager;
import com.kavsdkexample.antivirus.base.sdk.impl.Utils;
import com.kavsdkexample.antivirus.folder_monitor.sdk.FolderMonitorManager;
import com.kavsdkexample.core.ui.UiProvider;
import com.kavsdkexample.core.app.utils.ThreadManager;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class FolderMonitorManagerImpl implements FolderMonitorManager,
                                                 FolderMonitorListenerV2,
                                                 FolderMonitorSuspiciousListener {
    private final static String TAG = FolderMonitorManagerImpl.class.getSimpleName();
    private final Context                    mContext;
    private final ThreadManager              mThreadManager;

    private final Object                     mMonitorsLock;
    private final Object                     mDelayedFoldersLock;

    private final ExecutorService            mExecutorService;
    private final UiProvider                 mUiProvider;

    private       Map<String, FolderMonitor> mMonitors;
    private       Set<String>                mDelayedFolders;

    private volatile boolean mCloudEnabled;
    private volatile boolean mCloudOnlyEnabled;
    private volatile boolean mDetectRiskwareEnabled;
    private volatile boolean mTryCureEnabled;
    private volatile long    mMaxFileSize;


    public FolderMonitorManagerImpl(@NonNull final Context         context,
                                    @NonNull final ThreadManager   threadManager,
                                    @NonNull final ExecutorService executorService,
                                    @NonNull final UiProvider      uiProvider) {
        mContext            = context;
        mThreadManager      = threadManager;
        mExecutorService    = executorService;
        mUiProvider         = uiProvider;
        mMonitors           = new HashMap<>();
        mDelayedFolders     = new HashSet<>();
        mMonitorsLock       = new Object();
        mDelayedFoldersLock = new Object();
    }

    @Override
    public void init() {
    }

    @Override
    public void setFoldersToMonitor(@NonNull Set<String> folders) {
        synchronized (mDelayedFoldersLock) {
            mDelayedFolders.addAll(folders);
        }
    }

    private void loadMonitor(final String path) {
        boolean hasKey;

        synchronized (mMonitorsLock) {
            hasKey = mMonitors.containsKey(path);
        }

        if (hasKey) {
            Log.d(TAG, String.format("Directory '%s' is already monitored", path));
            return;
        }
        FolderMonitor monitor;
        try {
            monitor = new FolderMonitorBuilder(path)
                .setFolderMonitorListener(this)
                .setFolderMonitorSuspiciousListener(this)
                .setScanUdsAllow(mCloudEnabled)
                .setScanUdsOnly(mCloudOnlyEnabled)
                .setSkipRiskware(!mDetectRiskwareEnabled)
                .setCureInfectedFiles(mTryCureEnabled)
                .setMaxFileSize(mMaxFileSize)
                .create();
        } catch (SdkLicenseViolationException e) {
            Log.e(TAG, "Failed to launch monitor [dir: " + path  + "] - license expired");
            return;
        } catch (IOException e) {
            Log.e(TAG, "Failed to launch monitor [dir: " + path + "] - io error: " + e.getMessage());
            return;
        }

        monitor.start();

        FolderMonitor oldMonitor;
        synchronized (mMonitorsLock) {
            oldMonitor = mMonitors.put(path, monitor);
        }
        if (oldMonitor != null) {
            oldMonitor.stop();
        }
    }

    private void updateMonitors(@NonNull FolderMonitorApplier applier) {
        List<FolderMonitor> monitors;
        synchronized (mMonitorsLock) {
            monitors = new ArrayList<>(mMonitors.values());
        }
        for (FolderMonitor folderMonitor : monitors) {
            applier.onUpdate(folderMonitor);
        }
    }

    @Override
    public void applyMonitorState(boolean enabled, @NonNull MonitorBaseManager.MonitorStateObserver observer) {

        mExecutorService.execute(() -> {
            if (enabled) {
                Set<String> delayedFolders;
                synchronized (mDelayedFoldersLock) {
                    delayedFolders = mDelayedFolders;
                    mDelayedFolders = new HashSet<>();
                }
                for (String path : delayedFolders) {
                    File dir = new File(path);
                    if (dir.exists() && dir.isDirectory()) {
                        loadMonitor(path);
                    } else {
                        boolean found = false;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            for (UriPermission uri : mContext.getContentResolver().getPersistedUriPermissions()) {
                                if (path.startsWith(uri.getUri().toString())) {
                                    loadMonitor(path);
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if (!found) {
                            Log.e(TAG, "Skipped [dir: " + dir.getAbsolutePath() + "] - exists: [" + dir.exists()
                                    + "], is folder: [" + dir.isDirectory() + "]");
                        }
                    }
                }
            } else {
                removeAllFolders();
            }
            mThreadManager.runOnUiThread(() -> observer.onMonitorState(enabled));
        });
    }

    @Override
    public boolean getMonitorState() {
        boolean active;

        synchronized (mMonitorsLock) {
            active = mMonitors.size() > 0;
        }
        return active;
    }

    @NonNull
    @Override
    public Set<String> getFoldersToMonitor() {
        Set<String> result;
        synchronized (mMonitorsLock) {
            result = new HashSet<>(mMonitors.keySet());
        }
        return result;
    }

    @Override
    public void addFolderToMonitor(@NonNull String folder) {
        loadMonitor(folder);
    }

    @Override
    public void removeFolderFromMonitor(@NonNull String folder) {
        FolderMonitor monitor;
        synchronized (mMonitorsLock) {
            monitor = mMonitors.remove(folder);
        }
        if (monitor != null) {
            monitor.stop();
        }
    }

    @Override
    public void removeAllFolders() {
        Collection<FolderMonitor> monitors;
        Map<String, FolderMonitor> newMonitorsMap = new HashMap<>();
        synchronized (mMonitorsLock) {
            monitors = mMonitors.values();
            mMonitors = newMonitorsMap;
        }

        for (FolderMonitor monitor : monitors) {
            monitor.stop();
        }
    }

    @Override
    public void applyCloudCheck(final boolean value) {
        mCloudEnabled = value;
        updateMonitors(folderMonitor -> folderMonitor.setScanUdsAllow(value));
    }

    @Override
    public void applyCloudOnlyCheck(final boolean value) {
        mCloudOnlyEnabled = value;
        updateMonitors(folderMonitor -> folderMonitor.setScanUdsOnly(value));
    }

    @Override
    public void applyCheckRiskware(final boolean value) {
        mDetectRiskwareEnabled = value;
        updateMonitors(folderMonitor -> folderMonitor.setSkipRiskware(!value));
    }

    @Override
    public void applySuspiciousCheck(final boolean value) {
        // not applicable for this type of monitor
    }

    @Override
    public void applyCureInfected(final boolean value) {
        mTryCureEnabled = value;
        updateMonitors(folderMonitor -> folderMonitor.setCureInfectedFiles(value));
    }

    public void applyMaxFileCheckSize(final long maxFileSize) {
        mMaxFileSize = maxFileSize;
        updateMonitors(folderMonitor -> folderMonitor.setMaxFileSize(maxFileSize));
    }

    @Override
    public void onSuspiciousDetected(@NonNull ThreatInfo threatInfo,
                                     @SuppressWarnings("deprecation") SuspiciousThreatType threatType) {
        final String msg = "onSuspiciousDetected, threatName = " + threatInfo.getVirusName()
                + ", threat type = " + threatType.toString();
        Log.d(TAG, msg);
        mThreadManager.runOnUiThread(() -> mUiProvider.showToast(msg));
    }

    @Override
    public boolean onVirusDetected(ThreatInfo threatInfo, ThreatType threatType) {
        final String msg = "onVirusDetected, " + Utils.toString(threatInfo.getSeverityLevel()) + ", threatName = " + threatInfo.getVirusName();
        Log.d(TAG, msg);
        mThreadManager.runOnUiThread(() -> mUiProvider.showToast(msg));
        return true;
    }


    @Override
    public void onMonitorStop(FolderMonitor monitor) {
        synchronized (mMonitorsLock) {
            mMonitors.remove(monitor.getFolderPath());
        }
    }

    @Override
    public void onCleanFileScanned(@NonNull ThreatInfo cleanFileInfo, @NonNull ThreatType threatType) {
        Log.v(TAG, MessageFormat.format("File is clean: {0}", cleanFileInfo.getFileFullPath()));
    }

    @Override
    public void onMaxFileSizeExceeded(String path) {
        Log.v(TAG, MessageFormat.format("File is too big to be scanned: {0}", path));
    }


    /************************************* Inner classes *****************************************/
    private interface FolderMonitorApplier {
        void onUpdate(FolderMonitor folderMonitor);
    }

}
