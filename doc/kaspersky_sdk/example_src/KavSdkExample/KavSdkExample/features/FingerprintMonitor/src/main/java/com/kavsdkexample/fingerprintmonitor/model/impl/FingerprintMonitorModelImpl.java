/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.fingerprintmonitor.model.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import android.util.Log;

import com.kavsdkexample.core.ui.UiProvider;
import com.kavsdk.fingerprint.FingerprintMonitor;
import com.kavsdk.fingerprint.OnFingerprintChangedListener;
import com.kavsdkexample.core.app.model.impl.BaseModelImpl;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.fingerprintmonitor.model.FingerprintMonitorModel;
import com.kavsdkexample.fingerprintmonitor.model.Settings;
import com.kavsdkexample.fingerprintmonitor.R;


@UiThread
public class FingerprintMonitorModelImpl extends BaseModelImpl implements FingerprintMonitorModel {

    private final static String TAG = FingerprintMonitorModelImpl.class.getSimpleName();

    private final ThreadManager mThreadManager;
    private final Context mContext;
    private final UiProvider mUiProvider;

    private FingerprintMonitor mMonitor;
    private final Settings mSettings;

    public FingerprintMonitorModelImpl(@NonNull ThreadManager threadManager,
                                       @NonNull Settings settings,
                                       @NonNull UiProvider uiProvider,
                                       @NonNull Context context) {
        mThreadManager = threadManager;
        mContext = context;
        mSettings = settings;
        mUiProvider = uiProvider;
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public UiProvider getUiProvider() {
        return mUiProvider;
    }

    @Override
    public void onSdkInited() {
        super.onSdkInited();

        if (isEnabled()) {
            enableFingerprintMonitor();
        }
    }

    private boolean checkFingerprintMonitor() {
        try {
            if (mMonitor == null) {
                synchronized(this) {
                    if (mMonitor == null) {
                        FingerprintMonitor.init(mContext);
                        mMonitor = FingerprintMonitor.getInstance();
                    }
                }
            }
            return mMonitor.isAvailable();
        } catch (Exception e) {
            Log.e(TAG, "checkFingerprintMonitor", e);
            return false;
        }
    }

    private void enableFingerprintMonitor() {
        boolean available = checkFingerprintMonitor();
        if (available) {
            mMonitor.enable(() -> {
                Log.d(TAG, mContext.getString(R.string.str_fingerprint_monitor_fingerprint_changed_msg));
                mUiProvider.showToast(mContext.getString(R.string.str_fingerprint_monitor_fingerprint_changed_msg));
            });
        }
    }

    @Override
    public void enable() {
        if (!isEnabled()) {
            enableFingerprintMonitor();
            if (mMonitor != null && mMonitor.isEnabled()) {
                mSettings.saveMonitorEnabled(true);
            }
        }
    }

    @Override
    public void disable() {
        if (isEnabled()) {
            mSettings.saveMonitorEnabled(false);
            if (mMonitor != null) {
                mMonitor.disable();
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return mSettings.getMonitorEnabled();
    }

}
