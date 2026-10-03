/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.fingerprintmonitor.presenter;

import com.kavsdkexample.core.app.presenter.BasePresenter;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.fingerprintmonitor.view.FingerprintMonitorView;

public interface FingerprintMonitorPresenter extends BasePresenter<FingerprintMonitorView, BaseViewState> {
    void setMonitorEnabled(boolean enabled);
    boolean getMonitorEnabled();
}
