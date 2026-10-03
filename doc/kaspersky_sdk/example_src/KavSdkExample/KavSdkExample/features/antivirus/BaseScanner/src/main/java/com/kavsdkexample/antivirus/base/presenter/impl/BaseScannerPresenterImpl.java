/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.presenter.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.antivirus.base.model.AvAction;
import com.kavsdkexample.antivirus.base.model.BaseScannerModel;
import com.kavsdkexample.antivirus.base.model.BaseScannerModelObserver;
import com.kavsdkexample.antivirus.base.model.ScanObserver;
import com.kavsdkexample.antivirus.base.model.ScanResults;
import com.kavsdkexample.antivirus.base.presenter.BaseScannerPresenter;
import com.kavsdkexample.antivirus.base.view.ScannerBaseView;
import com.kavsdkexample.core.app.view.BaseViewState;

public class BaseScannerPresenterImpl<VIEW        extends ScannerBaseView<SCANRESULTS>,
                                      VIEWSTATE   extends BaseViewState,
                                      MODEL       extends BaseScannerModel<SCANRESULTS>,
                                      SCANRESULTS extends ScanResults>
                             extends    AntivirusBasePresenterImpl<VIEW, VIEWSTATE, MODEL>
                             implements BaseScannerPresenter<VIEW, VIEWSTATE> {


    protected BaseScannerPresenterImpl(@NonNull MODEL model) {
        super(model);
    }

    @Override
    public void subscribe(@NonNull VIEW view, @Nullable VIEWSTATE state, boolean viewCreated) {
        super.subscribe(view, state, viewCreated);
        registerModelObserver(new BaseScannerModelObserverImpl<>(view, mModel));
        if (viewCreated) {
            boolean isScanRunning = mModel.isScanRunning();
            view.setAvAction(mModel.getAvAction());
            view.setScanButtonState(isScanRunning ? ScannerBaseView.ScanButtonState.Stop : ScannerBaseView.ScanButtonState.Start);
            view.setPauseButtonState(
                    isScanRunning ?
                           mModel.isScanPaused() ?
                                    ScannerBaseView.PauseButtonState.Resume
                                  : ScannerBaseView.PauseButtonState.Pause
                       : ScannerBaseView.PauseButtonState.Disabled
            );
        }
    }

    @Override
    public void processScanButtonClick() {
        if (mModel.isScanRunning()) {
            mModel.stopScan();
            if (mView != null) {
                mView.setScanButtonState(ScannerBaseView.ScanButtonState.Stopping);
            }
        } else {
            if (mModel.startScan() && mView != null) {
                mView.setScanButtonState(ScannerBaseView.ScanButtonState.Stop);
                mView.setPauseButtonState(ScannerBaseView.PauseButtonState.Pause);
            }
        }
    }

    @Override
    public void processPauseResumeButtonClick() {
        if (mModel.isScanPaused()) {
            mModel.resumeScan();
            if (mView != null) {
                mView.setPauseButtonState(ScannerBaseView.PauseButtonState.Pause);
            }
        } else {
            mModel.pauseScan();
            if (mView != null) {
                mView.setPauseButtonState(ScannerBaseView.PauseButtonState.Resume);
            }
        }
    }

    @Override
    public void setAvAction(@NonNull AvAction action) {
        mModel.setAvAction(action);
    }

    private static class BaseScannerModelObserverImpl<VIEW        extends ScannerBaseView<SCANRESULTS>,
                                                      MODEL       extends BaseScannerModel<SCANRESULTS>,
                                                      SCANRESULTS extends ScanResults>
                                           implements BaseScannerModelObserver<SCANRESULTS> {
        private final VIEW mView;
        private final MODEL mModel;

        BaseScannerModelObserverImpl(@NonNull VIEW view, @NonNull MODEL model) {
            mView  = view;
            mModel = model;
        }

        @Override
        public void onBasesUnavailable() {
            mView.setScanButtonState(ScannerBaseView.ScanButtonState.Start);
            mView.showBasesUnavailable();
        }

        @Override
        public void onError(@NonNull ScanObserver.ScanErrorType error) {
            mView.setScanButtonState(ScannerBaseView.ScanButtonState.Start);
            mView.showError(error);
        }

        @Override
        public void onScanPaused() {
            mView.setPauseButtonState(ScannerBaseView.PauseButtonState.Resume);
        }

        @Override
        public void onScanResumed() {
            mView.setPauseButtonState(ScannerBaseView.PauseButtonState.Pause);
        }

        @Override
        public void onScanFinished(@NonNull SCANRESULTS scanResults) {
            mView.showScanResults(scanResults);
            mView.setScanButtonState(ScannerBaseView.ScanButtonState.Start);
            mModel.switchToScanResultsView();
            mModel.resetDisplayScanResults();
        }
    }
}
