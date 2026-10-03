/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.easy_scanner.presenter.impl;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.presenter.impl.ScanResultsPresenterImpl;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatInfoProvider;
import com.kavsdkexample.antivirus.base.view.ScanResultsView;
import com.kavsdkexample.antivirus.easy_scanner.model.EasyScanResults;
import com.kavsdkexample.antivirus.easy_scanner.model.EasyScannerModel;
import com.kavsdkexample.antivirus.easy_scanner.presenter.EasyScanResultsPresenter;
import com.kavsdkexample.core.app.view.BaseViewState;

import javax.inject.Inject;

public final class EasyScanResultsPresenterImpl extends    ScanResultsPresenterImpl<ScanResultsView<EasyScanResults>,
                                                                                    BaseViewState,
                                                                                    EasyScannerModel,
                                                                                    EasyScanResults>
                                                implements EasyScanResultsPresenter {
    @Inject
    EasyScanResultsPresenterImpl(@NonNull EasyScannerModel model, ThreatInfoProvider threatInfoProvider) {
        super(model, threatInfoProvider);
    }
}
