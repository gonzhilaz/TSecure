/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.easy_scanner.presenter;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.kavsdkexample.antivirus.base.presenter.BaseScannerPresenter;
import com.kavsdkexample.antivirus.easy_scanner.model.EasyScannerMode;
import com.kavsdkexample.antivirus.easy_scanner.view.EasyScannerView;
import com.kavsdkexample.core.app.view.BaseViewState;

@UiThread
public interface EasyScannerPresenter extends BaseScannerPresenter<EasyScannerView, BaseViewState> {
    void            setEasyScannerMode(@NonNull EasyScannerMode mode);
    @NonNull
    EasyScannerMode getEasyScannerMode();
}
