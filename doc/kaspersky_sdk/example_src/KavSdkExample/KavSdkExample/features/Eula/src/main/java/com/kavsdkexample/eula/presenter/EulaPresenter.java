/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.eula.presenter;

import androidx.annotation.UiThread;

import com.kavsdkexample.core.app.presenter.BasePresenter;
import com.kavsdkexample.eula.view.EulaView;
import com.kavsdkexample.eula.view.EulaViewState;

@UiThread
public interface EulaPresenter extends BasePresenter<EulaView, EulaViewState> {
    void acceptEula();
    void declineEula();
}
