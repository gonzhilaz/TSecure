/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.presenter.base.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.core.app.presenter.impl.BasePresenterImpl;
import com.kavsdkexample.secure_storage.model.base.SecureStorageBaseModel;
import com.kavsdkexample.secure_storage.presenter.base.SecureStoragePresenter;
import com.kavsdkexample.secure_storage.view.SecureStorageBaseView;
import com.kavsdkexample.secure_storage.view.SecureStorageBaseViewState;

public abstract class SecureStoragePresenterImpl<VIEW    extends    SecureStorageBaseView,
                                               VIEWSTATE extends    SecureStorageBaseViewState,
                                               MODEL     extends    SecureStorageBaseModel>
                                                         extends    BasePresenterImpl<VIEW, VIEWSTATE, MODEL>
                                                         implements SecureStoragePresenter<VIEW ,VIEWSTATE> {

    protected SecureStoragePresenterImpl(@NonNull MODEL model) {
        super(model);
    }

    @Override
    public void subscribe(@NonNull VIEW view, @Nullable VIEWSTATE state, boolean viewCreated) {
        super.subscribe(view, state, viewCreated);
        if (viewCreated) {
            if (!mModel.isInitialized()) {
                view.setOperationsButtonsEnabled(false);
            }

            if (state != null) {
                view.setPassword(state.getPassword());
            }
        }
    }

    @Override
    public void viewPaused(@NonNull VIEW view) {}

    @Override
    public void onSdkInited() {
        if (mView != null) {
            mView.setOperationsButtonsEnabled(true);
        }
    }

    @Override
    public void acceptNotice() {
    }

    @Override
    public void rejectNotice() {
    }
}
