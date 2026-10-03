/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.monitor.presenter;

import com.kavsdkexample.antivirus.base.monitor.view.MonitorBaseView;
import com.kavsdkexample.antivirus.base.presenter.AntivirusBasePresenter;
import com.kavsdkexample.core.app.view.BaseViewState;

public interface MonitorBasePresenter<VIEW      extends MonitorBaseView,
                                      VIEWSTATE extends BaseViewState>
                              extends AntivirusBasePresenter<VIEW, VIEWSTATE> {
    void enableMonitor(boolean value);
    void enableCloudOnlyCheck(boolean value);
    void enableCloudCheck(boolean value);
    void enableRiskwareCheck(boolean value);
    void enableSuspicious(boolean value);
    void setMaxFileCheckSize(long size);
}
