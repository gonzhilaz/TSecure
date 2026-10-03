/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.appcontrol.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.appcontrol.presenter.AppControlPresenter;
import com.kavsdkexample.core.app.view.BaseView;

public interface AppControlView extends BaseView {
    void onPackageNameNotAllowedInBlockList(@Nullable String packageName);
    void onPackageNameNotAllowedInAllowList(@Nullable String packageName);
    void requestRemovalConfirmation(@NonNull AppControlPresenter.RemovalConfirmationListener listener);
    void onIndexNotAllowedToRemoveFromBlockList(int index);
    void onIndexNotAllowedToRemoveFromAllowList(int index);
}