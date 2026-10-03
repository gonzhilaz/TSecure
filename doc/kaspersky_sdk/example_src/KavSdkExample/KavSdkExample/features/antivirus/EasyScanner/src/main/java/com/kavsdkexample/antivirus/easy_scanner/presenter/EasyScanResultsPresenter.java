/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.easy_scanner.presenter;

import com.kavsdkexample.antivirus.base.presenter.ScanResultsPresenter;
import com.kavsdkexample.antivirus.base.view.ScanResultsView;
import com.kavsdkexample.antivirus.easy_scanner.model.EasyScanResults;
import com.kavsdkexample.core.app.view.BaseViewState;

public interface EasyScanResultsPresenter extends ScanResultsPresenter<ScanResultsView<EasyScanResults>,
                                                                       BaseViewState,
                                                                       EasyScanResults> {
}
