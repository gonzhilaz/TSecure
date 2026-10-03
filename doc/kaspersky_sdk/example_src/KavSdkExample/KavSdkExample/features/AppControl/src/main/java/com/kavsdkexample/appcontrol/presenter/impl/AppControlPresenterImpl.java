/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.appcontrol.presenter.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.appcontrol.model.AppControlCategory;
import com.kavsdkexample.appcontrol.model.AppControlMode;
import com.kavsdkexample.appcontrol.model.AppControlModel;
import com.kavsdkexample.appcontrol.model.AppControlObserver;
import com.kavsdkexample.appcontrol.presenter.AppControlPresenter;
import com.kavsdkexample.appcontrol.view.AppControlView;
import com.kavsdkexample.core.app.presenter.impl.BasePresenterImpl;
import com.kavsdkexample.core.app.view.BaseViewState;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public final class AppControlPresenterImpl extends BasePresenterImpl<AppControlView, BaseViewState, AppControlModel>
        implements AppControlPresenter {

    private final AppControlObserver mAppControlObserver;

    @Inject
    AppControlPresenterImpl(@NonNull final AppControlModel model,
                            @NonNull final AppControlObserver appControlObserver) {
        super(model);
        mAppControlObserver = appControlObserver;
    }

    @Override
    public void subscribe(@NonNull AppControlView view, @Nullable BaseViewState state, boolean viewCreated) {
        super.subscribe(view, state, viewCreated);
        registerModelObserver(this);
    }

    @Override
    public void enableAppControl(boolean isChecked) {
        mModel.enableAppControl(isChecked);
    }

    @Override
    public void useWindowManagerForBlocking(boolean isUsed) {
        if (isUsed) {
            mModel.addAdditionalObserver(mAppControlObserver, true);
        } else {
            mModel.removeAdditionalObserver(mAppControlObserver, true);
        }
        mModel.useWindowManagerForBlocking(isUsed);
    }

    @Override
    public void setBothListsMode() {
        mModel.setMode(AppControlMode.BothLists);
    }

    @Override
    public void setBlockListMode() {
        mModel.setMode(AppControlMode.BlockList);
    }

    @Override
    public void setAllowListMode() {
        mModel.setMode(AppControlMode.AllowList);
    }

    @Override
    public void saveChanges() {
        mModel.saveChanges();
    }

    @Override
    public void openSettings() {
        mModel.openSettings();
    }

    @Override
    public List<String> getCategories() {
        List<String> categories = new ArrayList<String>();
        for (AppControlCategory category : AppControlCategory.values()) {
            categories.add(category.getName());
        }
        return categories;
    }

    @Override
    public void addItemToBlockList(final String packageName, final String category) {
        if (mModel.isAdditionAllowed(AppControlMode.BlockList, packageName)) {
            mModel.addItem(AppControlMode.BlockList, packageName, category);
        } else if (mView != null) {
            mView.onPackageNameNotAllowedInBlockList(packageName);
        }
    }

    @Override
    public void addItemToAllowList(final String packageName, final String category) {
        if (mModel.isAdditionAllowed(AppControlMode.AllowList, packageName)) {
            mModel.addItem(AppControlMode.AllowList, packageName, category);
        } else if (mView != null) {
            mView.onPackageNameNotAllowedInAllowList(packageName);
        }
    }

    @Override
    public int getBlockListItemsCount() {
        return mModel.getItemsCount(AppControlMode.BlockList);
    }

    @Override
    public int getAllowListItemsCount() {
        return mModel.getItemsCount(AppControlMode.AllowList);
    }

    @Override
    public String getBlockListPackage(int index) {
        return mModel.getPackage(AppControlMode.BlockList, index);
    }

    @Override
    public String getAllowListPackage(int index) {
        return mModel.getPackage(AppControlMode.AllowList, index);
    }

    @Override
    public String getBlockListCategory(int index) {
        return mModel.getCategory(AppControlMode.BlockList, index);
    }

    @Override
    public String getAllowListCategory(int index) {
        return mModel.getCategory(AppControlMode.AllowList, index);
    }

    @Override
    public boolean isAppcontrolEnabled() {
        return mModel.isAppcontrolEnabled();
    }

    @Override
    public boolean isWindowManagerForBlockingUsed() {
        return mModel.isWindowManagerForBlockingUsed();
    }

    @Override
    public void requestRemovalFromBlockList(final int index) {
        if (mView != null) {
            if (mModel.isRemovalAllowed(AppControlMode.BlockList, index)) {
                mView.requestRemovalConfirmation(
                        () -> mModel.deleteItem(AppControlMode.BlockList, index)
                );
            } else {
                mView.onIndexNotAllowedToRemoveFromBlockList(index);
            }
        }
    }

    @Override
    public void requestRemovalFromAllowList(final int index) {
        if (mView != null) {
            if (mModel.isRemovalAllowed(AppControlMode.AllowList, index)) {
                mView.requestRemovalConfirmation(
                        () -> mModel.deleteItem(AppControlMode.AllowList, index)
                );
            } else {
                mView.onIndexNotAllowedToRemoveFromAllowList(index);
            }
        }
    }
}