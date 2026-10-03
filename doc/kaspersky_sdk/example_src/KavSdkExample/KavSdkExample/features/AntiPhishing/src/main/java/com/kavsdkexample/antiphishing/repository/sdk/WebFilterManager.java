/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antiphishing.repository.sdk;

public interface WebFilterManager {
    int     getExclusionsCount();
    String  getExclusionAt(int index);
    void enableWebFiltering(boolean isEnabled);
    void enableExtCategories(boolean isEnabled);
    void enableIgnorePowerSaveMode(boolean isEnabled);
    boolean isWebFilterInitialised();
    CharSequence[] getCategoryNames();
    boolean[] getCheckedItems();
    void setCategoryEnabled(int which, boolean isChecked);
    void saveCategories();
    void openAccessibilitySettings();
    void restoreWifiProxySettings();
    void addExclusion(String url);
    void saveExclusions();
    void removeExclusion(int index);
}
