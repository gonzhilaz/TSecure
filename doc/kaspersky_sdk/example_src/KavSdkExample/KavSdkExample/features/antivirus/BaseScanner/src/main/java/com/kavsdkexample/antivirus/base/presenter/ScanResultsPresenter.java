/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.presenter;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.model.ScanResults;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatType;
import com.kavsdkexample.antivirus.base.view.ScanResultsView;
import com.kavsdkexample.core.app.view.BaseViewState;

public interface ScanResultsPresenter<VIEW        extends ScanResultsView<SCANRESULTS>,
                                      VIEWSTATE   extends BaseViewState,
                                      SCANRESULTS extends ScanResults>
                              extends AntivirusBasePresenter<VIEW, VIEWSTATE> {

    void switchToThreatsInfo(@NonNull ThreatType threatType);

    void switchToApplicationThreats();

    boolean hasApplicationThreats();

    void dismiss();
}
