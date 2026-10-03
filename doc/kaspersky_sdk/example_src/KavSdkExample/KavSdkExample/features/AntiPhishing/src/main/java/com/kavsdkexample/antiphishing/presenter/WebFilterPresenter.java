/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antiphishing.presenter;

import androidx.annotation.Nullable;

import com.kavsdkexample.antiphishing.view.WebFilterView;
import com.kavsdkexample.core.app.presenter.BasePresenter;
import com.kavsdkexample.core.app.view.BaseViewState;

public interface WebFilterPresenter extends BasePresenter<WebFilterView, BaseViewState> {
    int getExclusionsCount();
    @Nullable
    String getExclusionAt(int index);
    void enableWebFiltering(boolean isEnabled);
    void enableExtCategories(boolean isEnabled);
    void enableIgnorePowerSaveMode(boolean isEnabled);
    void enableWifiProxy(int port, boolean isEnabled);
    boolean getSavedWifiProxy();
    void onPostCreate(WebFilterView view);
    CharSequence[] getCategoryNames();
    boolean[] getCheckedItems();
    void setCategoryEnabled(int which, boolean isChecked);
    void saveCategories();
    void openAccessibilitySettings();
    void addExclusion(String url);
    void saveExclusions();
    void removeExclusion(int index);
}
