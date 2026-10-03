/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.presenter.proxy;

import com.kavsdkexample.core.app.presenter.BasePresenter;
import com.kavsdkexample.view.sdk.impl.ProxyAuthView;
import com.kavsdkexample.view.sdk.impl.ProxyAuthViewState;

public interface ProxyAuthPresenter extends BasePresenter<ProxyAuthView, ProxyAuthViewState> {
    void setProxyAuthCredentials(String login, String password);
    void cancelProxyAuth();
}
