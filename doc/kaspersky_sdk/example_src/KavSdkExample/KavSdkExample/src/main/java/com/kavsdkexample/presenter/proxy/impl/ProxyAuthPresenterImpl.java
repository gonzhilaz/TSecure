/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.presenter.proxy.impl;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.presenter.impl.BasePresenterImpl;
import com.kavsdkexample.model.AppModel;
import com.kavsdkexample.presenter.proxy.ProxyAuthPresenter;
import com.kavsdkexample.view.sdk.impl.ProxyAuthView;
import com.kavsdkexample.view.sdk.impl.ProxyAuthViewState;

import javax.inject.Inject;

public class ProxyAuthPresenterImpl extends BasePresenterImpl<ProxyAuthView, ProxyAuthViewState, AppModel>
        implements ProxyAuthPresenter {

    @Inject
    ProxyAuthPresenterImpl(@NonNull AppModel model) {
        super(model);
    }

    @Override
    public void setProxyAuthCredentials(String login, String password) {
        mModel.setProxyAuthCredentials(login, password);
    }

    @Override
    public void cancelProxyAuth() {
        mModel.cancelProxyAuth();
    }
}
