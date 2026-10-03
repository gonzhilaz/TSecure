/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_connectivity.presenter.impl;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.presenter.impl.BasePresenterImpl;
import com.kavsdkexample.core.app.view.BaseView;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.secure_connectivity.model.SecureConnectionModel;
import com.kavsdkexample.secure_connectivity.presenter.SecureConnectionPresenter;

import javax.inject.Inject;

public class SecureConnectionPresenterImpl extends    BasePresenterImpl<BaseView, BaseViewState, SecureConnectionModel>
                                           implements SecureConnectionPresenter {

    @Inject
    SecureConnectionPresenterImpl(@NonNull SecureConnectionModel model) {
        super(model);
    }
}
