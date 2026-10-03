/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.presenter;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.model.AvAction;
import com.kavsdkexample.antivirus.base.view.AntivirusBaseView;
import com.kavsdkexample.core.app.view.BaseViewState;

public interface BaseScannerPresenter<VIEW      extends AntivirusBaseView,
                                      VIEWSTATE extends BaseViewState>
                               extends AntivirusBasePresenter<VIEW, VIEWSTATE> {
    void setAvAction(@NonNull AvAction action);
    void processScanButtonClick();
    void processPauseResumeButtonClick();
}
