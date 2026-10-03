/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.view.sdk.impl;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.kavsdk.ProxyAuthRequestListener;

/**
 * This class receives callback from SDK if proxy authorization is required
 */
public class ProxyAuthActivityLauncher implements ProxyAuthRequestListener {
    private final Context mContext;

    public ProxyAuthActivityLauncher(@NonNull Context context) {
        mContext = context;
    }

    @Override
    @WorkerThread
    public void onProxyAuthRequired() {
        final Intent intent = new Intent(mContext, ProxyAuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        mContext.startActivity(intent);
    }
}
