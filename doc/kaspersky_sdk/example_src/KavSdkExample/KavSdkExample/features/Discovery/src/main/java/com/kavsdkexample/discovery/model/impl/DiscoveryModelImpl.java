/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.discovery.model.impl;

import com.kavsdkexample.core.app.model.impl.BaseModelImpl;
import com.kavsdkexample.core.app.utils.ThreadManager;

import com.kavsdkexample.discovery.model.DiscoveryModel;
import com.kavsdkexample.discovery.sdk.SdkDiscovery;
import com.kavsdkexample.discovery.sdk.SdkDiscoveryListener;

public final class DiscoveryModelImpl extends BaseModelImpl implements DiscoveryModel, SdkDiscoveryListener {

    private final ThreadManager mThreadManager;
    private final SdkDiscovery mSdkDiscovery;
    private final SdkDiscoveryListener mSdkDiscoveryListener;

    public DiscoveryModelImpl(ThreadManager threadManager, SdkDiscovery sdkDiscovery, SdkDiscoveryListener sdkDiscoveryListener) {
        mThreadManager = threadManager;
        mSdkDiscovery = sdkDiscovery;
        mSdkDiscoveryListener = sdkDiscoveryListener;
    }

    @Override
    public void onSdkInited() {
        super.onSdkInited();
        mSdkDiscovery.setListener(this);
    }

    @Override
    public void onSegmentChanged(final String segment) {
        mThreadManager.runOnUiThread(() -> mSdkDiscoveryListener.onSegmentChanged(segment) );
    }

}
