/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.single_thread_scanner.presenter;

import com.kavsdkexample.antivirus.base.presenter.ScanResultsPresenter;
import com.kavsdkexample.antivirus.base.view.ScanResultsView;
import com.kavsdkexample.antivirus.single_thread_scanner.model.SingleThreadScanResults;
import com.kavsdkexample.core.app.view.BaseViewState;

public interface SingleThreadScanResultsPresenter extends ScanResultsPresenter<ScanResultsView<SingleThreadScanResults>,
                                                                               BaseViewState,
                                                                               SingleThreadScanResults> {
}
