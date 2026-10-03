/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.pua_scanner.presenter.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.antivirus.base.model.ScanResults;
import com.kavsdkexample.antivirus.base.presenter.impl.BaseScannerPresenterImpl;
import com.kavsdkexample.antivirus.pua_scanner.model.PuaScannerModel;
import com.kavsdkexample.antivirus.pua_scanner.presenter.PuaScanPresenter;
import com.kavsdkexample.antivirus.pua_scanner.view.PuaScanView;
import com.kavsdkexample.core.app.view.BaseViewState;

import javax.inject.Inject;

public class PuaScanPresenterImpl extends    BaseScannerPresenterImpl<PuaScanView,
                                                                      BaseViewState,
                                                                      PuaScannerModel,
                                                                      ScanResults>
                                  implements PuaScanPresenter {

    @Inject
    PuaScanPresenterImpl(@NonNull PuaScannerModel model) {
        super(model);
    }

    @Override
    public void subscribe(@NonNull PuaScanView view, @Nullable BaseViewState state, boolean viewCreated) {
        super.subscribe(view, state, viewCreated);
    }
}
