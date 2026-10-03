/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.discovery.sdk.impl;

import android.util.Log;

import com.kavsdk.discovery.DiscoveryListener;
import com.kavsdk.discovery.DiscoveryService;

import com.kavsdkexample.discovery.sdk.SdkDiscovery;
import com.kavsdkexample.discovery.sdk.SdkDiscoveryListener;

public class SdkDiscoveryImpl implements SdkDiscovery {

    private static final String TAG = SdkDiscoveryImpl.class.getSimpleName();

    @Override
    public void setListener(final SdkDiscoveryListener listener) {
        DiscoveryService.getDiscovery().setListener(new DiscoveryListener() {
            @Override
            public void onSegmentChanged(String segment) {
                Log.d(TAG, "onSegmentChanged: " + segment);
                listener.onSegmentChanged(segment);
            }
        });
    }

}
