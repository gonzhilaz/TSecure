/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.discovery.sdk.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import android.widget.Toast;

import com.kavsdkexample.discovery.sdk.SdkDiscoveryListener;
import com.kavsdkexample.discovery.R;

public class SdkDiscoveryListenerImpl implements SdkDiscoveryListener {

    private final Context mContext;

    public SdkDiscoveryListenerImpl(@NonNull Context context) {
        mContext = context;
    }

    @Override
    public void onSegmentChanged(String segment) {
        Toast.makeText(mContext, mContext.getString(R.string.str_discovery_toast), Toast.LENGTH_SHORT).show();
    }

}
