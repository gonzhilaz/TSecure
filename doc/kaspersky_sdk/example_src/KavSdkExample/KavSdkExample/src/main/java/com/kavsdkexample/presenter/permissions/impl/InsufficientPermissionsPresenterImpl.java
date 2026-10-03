/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.presenter.permissions.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.core.app.presenter.impl.BasePresenterImpl;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.model.AppModel;
import com.kavsdkexample.presenter.permissions.InsufficientPermissionsPresenter;
import com.kavsdkexample.view.permissions.InsufficientPermissionsView;

import javax.inject.Inject;

public class InsufficientPermissionsPresenterImpl extends    BasePresenterImpl<InsufficientPermissionsView, BaseViewState, AppModel>
                                                  implements InsufficientPermissionsPresenter {
    @Inject
    InsufficientPermissionsPresenterImpl(@NonNull AppModel model) {
        super(model);
    }

    @Override
    public void subscribe(@NonNull InsufficientPermissionsView view,
                          @Nullable BaseViewState state,
                          boolean viewCreated) {
        super.subscribe(view, state, viewCreated);
        if (viewCreated) {
            view.showInsufficientPermissions(mModel.getInsufficientPermissions());
        }
    }

    @Override
    public void tryAgain() {
        mModel.setAllPermissionsUnchecked();
        mModel.checkPermissions();
    }

    @Override
    public void proceed() {
        mModel.ignoreInsufficientPermissions();
    }
}
