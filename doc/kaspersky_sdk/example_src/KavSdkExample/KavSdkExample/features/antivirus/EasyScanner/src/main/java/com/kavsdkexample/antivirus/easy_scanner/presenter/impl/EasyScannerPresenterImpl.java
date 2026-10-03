/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.easy_scanner.presenter.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.kavsdkexample.antivirus.base.presenter.impl.BaseScannerPresenterImpl;
import com.kavsdkexample.antivirus.easy_scanner.model.EasyScanResults;
import com.kavsdkexample.antivirus.easy_scanner.model.EasyScannerMode;
import com.kavsdkexample.antivirus.easy_scanner.model.EasyScannerModel;
import com.kavsdkexample.antivirus.easy_scanner.presenter.EasyScannerPresenter;
import com.kavsdkexample.antivirus.easy_scanner.view.EasyScannerView;
import com.kavsdkexample.core.app.view.BaseViewState;

import javax.inject.Inject;

@UiThread
public final class EasyScannerPresenterImpl extends    BaseScannerPresenterImpl<EasyScannerView,
                                                                                BaseViewState,
                                                                                EasyScannerModel,
                                                                                EasyScanResults>
                                            implements EasyScannerPresenter {
    @Inject
    EasyScannerPresenterImpl(@NonNull EasyScannerModel model) {
        super(model);
    }

    @Override
    public void subscribe(@NonNull EasyScannerView view, @Nullable BaseViewState state, boolean viewCreated) {
        super.subscribe(view, state, viewCreated);
        if (viewCreated) {
            view.setEasyScannerMode(mModel.getEasyScannerMode());
        }
    }

    @Override
    public void setEasyScannerMode(@NonNull EasyScannerMode mode) {
        mModel.setEasyScannerMode(mode);
    }

    @NonNull
    @Override
    public EasyScannerMode getEasyScannerMode() {
        return mModel.getEasyScannerMode();
    }
}
