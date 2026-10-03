/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.easy_scanner.view.impl;

import com.kavsdkexample.antivirus.base.model.ScanResults;
import com.kavsdkexample.antivirus.base.presenter.ScanResultsPresenter;
import com.kavsdkexample.antivirus.base.view.ScanResultsView;
import com.kavsdkexample.antivirus.base.view.impl.AntivirusBaseFragment;
import com.kavsdkexample.antivirus.base.view.impl.OdsScannerMainFragment;
import com.kavsdkexample.antivirus.base.view.impl.ScanResultsFragment;
import com.kavsdkexample.core.app.presenter.BasePresenter;
import com.kavsdkexample.core.app.view.BaseView;
import com.kavsdkexample.core.app.view.BaseViewState;

public class EasyScannerMainFragment extends OdsScannerMainFragment {

    protected Class<? extends AntivirusBaseFragment<? extends BaseView,
                                                    ? extends BaseViewState,
                                                    ? extends BasePresenter>> getScannerFragmentClass() {
        return EasyScannerFragment.class;
    }

    protected Class<? extends ScanResultsFragment<? extends ScanResultsView<? extends ScanResults>,
                                                  ? extends BaseViewState,
                                                  ? extends ScanResultsPresenter<
                                                  ? extends ScanResultsView<? extends ScanResults>,
                                                                            ? extends BaseViewState,
                                                                            ? extends ScanResults>,
                                                  ? extends ScanResults>> getScanResultsFragmentClass() {
        return EasyScanResultsFragment.class;
    }
}
