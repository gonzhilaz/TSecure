/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.single_thread_scanner.presenter.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.antivirus.base.presenter.impl.BaseOdsScannerPresenterImpl;
import com.kavsdkexample.antivirus.single_thread_scanner.model.SingleThreadScanResults;
import com.kavsdkexample.antivirus.single_thread_scanner.model.SingleThreadScannerModel;
import com.kavsdkexample.antivirus.single_thread_scanner.presenter.SingleThreadScannerPresenter;
import com.kavsdkexample.antivirus.single_thread_scanner.view.SingleThreadScannerView;
import com.kavsdkexample.core.app.view.BaseViewState;

import javax.inject.Inject;

public class SingleThreadScannerPresenterImpl extends    BaseOdsScannerPresenterImpl<SingleThreadScannerView,
                                                                                     BaseViewState,
                                                                                     SingleThreadScannerModel,
                                                                                     SingleThreadScanResults>
                                              implements SingleThreadScannerPresenter {

    @Inject
    SingleThreadScannerPresenterImpl(@NonNull SingleThreadScannerModel model) {
        super(model);
    }

    @Override
    public void subscribe(@NonNull SingleThreadScannerView view, @Nullable BaseViewState state, boolean viewCreated) {
        super.subscribe(view, state, viewCreated);
        if (viewCreated) {
            view.showSdCardsOptions(mModel.getSdCardsOptions());
            view.setCloudOnlyScan(mModel.getCloudOnlyScan());
            view.setAllowCloudScan(mModel.getAllowCloudScan());
            view.setScanSuspicious(mModel.getScanSuspicious());
            view.setDetectRiskwareAdware(mModel.getScanRiskware());
            view.setObjectsToScan(mModel.getObjectsToScanType(), mModel.getObjectsToScanPath());
            view.setTryToCure(mModel.getTryCure());
        }
    }

    @Override
    public void setTryCure(boolean enabled) {
        mModel.setTryCure(enabled);
    }
}
