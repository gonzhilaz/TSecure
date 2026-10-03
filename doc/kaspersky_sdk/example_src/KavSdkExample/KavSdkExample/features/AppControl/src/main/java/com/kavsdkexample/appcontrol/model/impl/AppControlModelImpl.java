/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.appcontrol.model.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.kavsdkexample.appcontrol.model.AppControlManager;
import com.kavsdkexample.appcontrol.model.AppControlMode;
import com.kavsdkexample.appcontrol.model.AppControlModel;
import com.kavsdkexample.appcontrol.model.AppControlObserver;
import com.kavsdkexample.appcontrol.model.Settings;
import com.kavsdkexample.core.app.model.impl.BaseModelImpl;

import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

@UiThread
public class AppControlModelImpl extends BaseModelImpl implements AppControlModel {
    private final Provider<AppControlManager> mAppControlManagerProvider;
    private final Settings mSettings;
    private final List<String> mMandatoryAllowedPackageNames;

    private AppControlManager mAppControlManager;

    public AppControlModelImpl(@NonNull final Settings settings,
                               @NonNull final List<String> mandatoryAllowedPackageNames,
                               @NonNull final Provider<AppControlManager> appControlManager) {
        mAppControlManagerProvider = appControlManager;
        mSettings = settings;
        mMandatoryAllowedPackageNames = Collections.unmodifiableList(
                mandatoryAllowedPackageNames
        );
    }

    @Override
    public void enableAppControl(boolean isChecked) {
        getAppControlManager().enableAppControl(isChecked);
        mSettings.saveAppcontrolButtonStatus(isChecked);
    }

    @Override
    public void useWindowManagerForBlocking(boolean isUsed) {
        getAppControlManager().useWindowManagerForBlocking(isUsed);
        mSettings.saveWindowManagerForBlockingButton(isUsed);
    }

    @Override
    public void setMode(AppControlMode mode) {
        getAppControlManager().setMode(mode);
    }

    @Override
    public void saveChanges() {
        getAppControlManager().saveChanges();
    }

    @Override
    public void openSettings() {
        getAppControlManager().openSettings();
    }

    @Override
    public void addItem(final AppControlMode mode, final String packageName, final String category) {
        getAppControlManager().addItem(mode, packageName, category);
    }

    @Override
    public int getItemsCount(final AppControlMode mode) {
        return getAppControlManager().getItemsCount(mode);
    }

    @Override
    public String getPackage(final AppControlMode mode, int index) {
        return getAppControlManager().getPackage(mode, index);
    }

    @Override
    public String getCategory(final AppControlMode mode, int index) {
        return getAppControlManager().getCategory(mode, index);
    }

    @Override
    public void deleteItem(final AppControlMode mode, int index) {
        getAppControlManager().deleteItem(mode, index);
    }

    @Override
    public void showDialog() {
        notifyObservers(AppControlObserver::showDialog, AppControlObserver.class);
    }

    @Override
    public boolean isAppcontrolEnabled() {
        return mSettings.getAppcontrolButtonStatus();
    }

    @Override
    public boolean isWindowManagerForBlockingUsed() {
        return mSettings.getWindowManagerForBlockingButton();
    }

    @Override
    public boolean isAdditionAllowed(@NonNull final AppControlMode appControlMode,
                                     @Nullable final String packageName) {
        // if changing implementation of this method,
        // also change implementation of AppControlActivity#onPackageNameNotAllowedInBlockList
        //                        and/or AppControlActivity#onPackageNameNotAllowedInAllowList
        return (appControlMode != AppControlMode.BlockList)
                || !mMandatoryAllowedPackageNames.contains(packageName);
    }

    @Override
    public boolean isRemovalAllowed(@NonNull final AppControlMode appControlMode, final int index) {
        // if changing implementation of this method,
        // also change implementation of AppControlActivity#onIndexNotAllowedToRemoveFromBlockList
        //                        and/or AppControlActivity#onIndexNotAllowedToRemoveFromAllowList
        return (appControlMode != AppControlMode.AllowList)
                || !mMandatoryAllowedPackageNames.contains(
                        getAppControlManager().getPackage(AppControlMode.AllowList, index)
        );
    }

    private AppControlManager getAppControlManager() {
        if (mAppControlManager == null) {
            mAppControlManager = mAppControlManagerProvider.get();
            mAppControlManager.setAppControlModel(this);
        }
        for (final String packageName: mMandatoryAllowedPackageNames) {
            if (!allowListContains(packageName)) {
                mAppControlManager.addItem(AppControlMode.AllowList, packageName, null);
            }
        }
        return mAppControlManager;
    }

    private boolean allowListContains(final String packageName) {
        for (int i = 0; i < mAppControlManager.getItemsCount(AppControlMode.AllowList); i++) {
            final String allowedPackage = mAppControlManager.getPackage(
                    AppControlMode.AllowList,
                    i
            );
            if (packageName.equals(allowedPackage)) {
                return true;
            }
        }
        return false;
    }
}