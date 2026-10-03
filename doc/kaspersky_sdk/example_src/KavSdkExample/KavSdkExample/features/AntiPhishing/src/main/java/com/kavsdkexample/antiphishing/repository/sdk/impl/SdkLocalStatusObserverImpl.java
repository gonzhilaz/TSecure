/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antiphishing.repository.sdk.impl;

import androidx.annotation.NonNull;

import com.kavsdk.KavSdk;
import com.kavsdk.sdkstatus.ComponentStatus;
import com.kavsdk.sdkstatus.SdkLocalStatus;
import com.kavsdkexample.antiphishing.repository.sdk.SdkLocalStatusObserver;
import com.kavsdkexample.antiphishing.model.WebFilterModel;
import com.kavsdkexample.core.app.utils.ThreadManager;

/**
 * This is the sample of local SDK status change handling.
 * When one of the component changes its status
 * {@link SdkLocalStatusObserverImpl#onLocalStatusChanged} method is called.
 * In this method you should get local SDK staus by calling {@link com.kavsdk.KavSdk#getLocalSdkStatus()}
 * and check the status of each component by calling corresponding method.
 * If the status of component is {@link com.kavsdk.sdkstatus.ComponentStatus#OK} than it works normally.
 * If the status of component is {@link com.kavsdk.sdkstatus.ComponentStatus#Off} than it switched off.
 * For more information see {@link com.kavsdk.sdkstatus.SdkLocalStatus}.
 */
public class SdkLocalStatusObserverImpl implements SdkLocalStatusObserver, com.kavsdk.sdkstatus.SdkLocalStatusObserver {
    private WebFilterModel mWebFilterModel;
    private final ThreadManager mThreadManager;

    public SdkLocalStatusObserverImpl(@NonNull WebFilterModel webFilterModel, @NonNull ThreadManager threadManager) {
        mWebFilterModel = webFilterModel;
        mThreadManager = threadManager;
    }

    @Override
    public void onLocalStatusChanged() {
        mThreadManager.runOnUiThread(() -> {
            SdkLocalStatus sdkLocalStatus = KavSdk.getLocalSdkStatus();
            ComponentStatus webfilterStatus = sdkLocalStatus.getWebfilterStatus();
            ComponentStatus taskReputationStatus = sdkLocalStatus.getTaskReputationStatus();
            if (webfilterStatus == ComponentStatus.SocketListeningError) {
                mWebFilterModel.changeProxyPort();
            }

            if (webfilterStatus != ComponentStatus.OK && webfilterStatus != ComponentStatus.Off) {
                mWebFilterModel.notificationOfWbFilterNotWorking();
            }
            if (taskReputationStatus != ComponentStatus.OK && taskReputationStatus != ComponentStatus.Off) {
                mWebFilterModel.notificationOfTaskReputationNotWorking();
            }
        });
    }
}