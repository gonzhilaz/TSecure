/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.presenter.impl;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.antivirus.base.model.BaseScannerModel;
import com.kavsdkexample.antivirus.base.model.ScanResults;
import com.kavsdkexample.antivirus.base.presenter.ScanResultsPresenter;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatFoundedBy;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatInfoProvider;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatType;
import com.kavsdkexample.antivirus.base.view.ScanResultsView;
import com.kavsdkexample.core.app.presenter.impl.BasePresenterImpl;
import com.kavsdkexample.core.app.view.BaseViewState;

public class ScanResultsPresenterImpl<VIEW        extends ScanResultsView<SCANRESULTS>,
                                      VIEWSTATE   extends BaseViewState,
                                      MODEL       extends BaseScannerModel<SCANRESULTS>,
                                      SCANRESULTS extends ScanResults>
                             extends    BasePresenterImpl<VIEW, VIEWSTATE, MODEL>
                             implements ScanResultsPresenter<VIEW, VIEWSTATE, SCANRESULTS> {

    final ThreatInfoProvider mThreatInfoProvider;

    protected ScanResultsPresenterImpl(@NonNull MODEL model, ThreatInfoProvider threatInfoProvider) {
        super(model);
        mThreatInfoProvider = threatInfoProvider;
    }

    @Override
    public void switchToThreatsInfo(@NonNull ThreatType threatType) {
        mModel.switchToThreatsInfoView(threatType);
    }

    @Override
    public void switchToApplicationThreats() {
        mModel.switchToOdsApplicationThreats();
    }

    @Override
    public boolean hasApplicationThreats() {
        Cursor cursor = mThreatInfoProvider.getApplicationThreats(ThreatType.AllAccurate, ThreatFoundedBy.Ods);
        if (cursor != null) {
            boolean hasFirst = cursor.moveToFirst();
            cursor.close();
            return hasFirst;
        } else {
            return false;
        }
    }

    @Override
    public void dismiss() {
        mModel.consumeScanResults();
        mModel.switchToScanView();
    }

    @Override
    public void subscribe(@NonNull VIEW view, @Nullable VIEWSTATE state, boolean viewCreated) {
        super.subscribe(view, state, viewCreated);
        SCANRESULTS scanResults = mModel.getScanResults();
        if (scanResults == null) {
            dismiss();
            return;
        }
        if (viewCreated || !view.isScanResultsShown()) {
            view.showScanResults(scanResults);
        }
    }
}
