/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antiphishing.view;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.view.BaseView;

public interface WebFilterView extends BaseView {
    void showInitError(@NonNull Exception e);
    void disableAll();
    void enableWebFiltering(boolean isEnabled);
    void enableExtCategories(boolean isEnabled);
    void enableIgnorePowerSaveMode(boolean isEnabled);
    void enableWifiProxy(boolean isEnabled);
    boolean isInitialised();
    void setProxyPort(int port);
    void notificationOfWbFilterNotWorking();
    void notificationOfTaskReputationNotWorking();
    void changeProxyPort();
    void updateExclusionList();
}
