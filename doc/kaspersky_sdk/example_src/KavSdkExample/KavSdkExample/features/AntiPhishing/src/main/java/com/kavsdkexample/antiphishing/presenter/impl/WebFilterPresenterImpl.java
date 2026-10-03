/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antiphishing.presenter.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.antiphishing.model.WebFilterModel;
import com.kavsdkexample.antiphishing.model.WebFilterModelObserver;
import com.kavsdkexample.antiphishing.view.WebFilterView;
import com.kavsdkexample.core.app.presenter.impl.BasePresenterImpl;
import com.kavsdkexample.antiphishing.presenter.WebFilterPresenter;
import com.kavsdkexample.core.app.view.BaseViewState;

import javax.inject.Inject;

public final class WebFilterPresenterImpl extends BasePresenterImpl<WebFilterView,
                                                                    BaseViewState,
                                                                    WebFilterModel>
                                          implements WebFilterPresenter,
                                                     WebFilterModelObserver {
    private final Object mGuard = new Object();
    private boolean mIsRestored;

    @Inject
    WebFilterPresenterImpl(@NonNull WebFilterModel model) {
        super(model);
    }

    @Override
    public void subscribe(@NonNull WebFilterView view, @Nullable BaseViewState state, boolean viewCreated) {
        super.subscribe(view, state, viewCreated);
        registerModelObserver(this);
    }

    @Override
    public int getExclusionsCount() {
        return mModel.getExclusionsCount();
    }

    @Nullable
    @Override
    public String getExclusionAt(int index) {
        return mModel.getExclusionAt(index);
    }

    @Override
    public void onWebFilterInitSuccess() {
        if (mView != null && mView.isInitialised()) {
            restore(mView);
        }
    }

    @Override
    public void onPostCreate(WebFilterView view) {
        if (mModel.isWebFilterInitialised()) {
            restore(view);
        }
    }

    @Override
    public void onWebFilterInitFailed(@NonNull Exception e) {
        if (mView != null) {
            mView.showInitError(e);
            mView.disableAll();
        }
    }

    @Override
    public void enableWebFiltering(boolean isEnabled) {
        mModel.enableWebFiltering(isEnabled);
        if (mView != null) {
            mView.enableWebFiltering(isEnabled);
        }
    }

    @Override
    public void enableExtCategories(boolean isEnabled) {
        mModel.enableExtCategories(isEnabled);
        mModel.saveCategories();
        if (mView != null) {
            mView.enableExtCategories(isEnabled);
        }
    }

    @Override
    public void enableIgnorePowerSaveMode(boolean isEnabled) {
        mModel.enableIgnorePowerSaveMode(isEnabled);
        if (mView != null) {
            mView.enableIgnorePowerSaveMode(isEnabled);
        }
    }

    @Override
    public void enableWifiProxy(int port, boolean isEnabled) {
        mModel.enableWifiProxy(port, isEnabled);
        if (mView != null) {
            mView.enableWifiProxy(isEnabled);
        }
    }

    private void restore(WebFilterView view) {
        boolean isRestored;
        synchronized (mGuard) {
            isRestored = mIsRestored;
            mIsRestored = true;
        }

        if (isRestored) {
            return;
        }

        view.enableWebFiltering(mModel.getSavedWebFilter());
        mModel.enableExtCategories(mModel.getSavedExtendedCategories());
        view.enableExtCategories(mModel.getSavedExtendedCategories());
        mModel.enableIgnorePowerSaveMode(mModel.getSavedIgnorePowerSafeMode());
        view.enableIgnorePowerSaveMode(mModel.getSavedIgnorePowerSafeMode());
        view.enableWifiProxy(mModel.getSavedWifiProxy());
        view.setProxyPort(mModel.getSavedProxyPort());
        view.updateExclusionList();
    }

    @Override
    public CharSequence[] getCategoryNames() {
        return mModel.getCategoryNames();
    }

    @Override
    public boolean[] getCheckedItems() {
        return mModel.getCheckedItems();
    }

    @Override
    public void setCategoryEnabled(int which, boolean isChecked) {
        mModel.setCategoryEnabled(which, isChecked);
    }

    @Override
    public void saveCategories() {
        mModel.saveCategories();
    }

    @Override
    public void openAccessibilitySettings() {
        mModel.openAccessibilitySettings();
    }

    @Override
    public void notificationOfWbFilterNotWorking() {
        if (mView != null) {
            mView.notificationOfWbFilterNotWorking();
        }
    }

    @Override
    public void notificationOfTaskReputationNotWorking() {
        if (mView != null) {
            mView.notificationOfWbFilterNotWorking();
        }
    }

    @Override
    public void changeProxyPort() {
        if (mView != null) {
            mView.changeProxyPort();
        }
    }

    @Override
    public void addExclusion(String url) {
        mModel.addExclusion(url);
    }

    @Override
    public void saveExclusions() {
        mModel.saveExclusions();
    }

    @Override
    public boolean getSavedWifiProxy() {
        return mModel.getSavedWifiProxy();
    }

    @Override
    public void removeExclusion(int index) {
        mModel.removeExclusion(index);
    }
}