/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antiphishing.model;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.model.BaseModel;
import com.kavsdkexample.core.app.model.interactors.NotificationInteractor;
import com.kavsdkexample.core.app.model.interactors.ServiceInteractor;

public interface WebFilterModel extends BaseModel {
    void initWebFilter();
    boolean getSavedWebFilter();
    boolean getSavedExtendedCategories();
    boolean getSavedIgnorePowerSafeMode();
    boolean getSavedWifiProxy();
    int getSavedProxyPort();
    int getExclusionsCount();
    String getExclusionAt(int index);
    void enableExtCategories(boolean isEnabled);
    void enableWebFiltering(boolean isEnabled);
    void enableIgnorePowerSaveMode(boolean isEnabled);
    void enableWifiProxy(int port, boolean isEnabled);
    boolean isWebFilterInitialised();
    CharSequence[] getCategoryNames();
    boolean[] getCheckedItems();
    void setCategoryEnabled(int which, boolean isChecked);
    void saveCategories();
    void openAccessibilitySettings();
    void changeProxyPort();
    void notificationOfWbFilterNotWorking();
    void notificationOfTaskReputationNotWorking();
    void addExclusion(String url);
    void saveExclusions();
    void removeExclusion(int index);
    void setInteractors(@NonNull ServiceInteractor serviceInteractor, @NonNull NotificationInteractor notificationInteractor);
}
