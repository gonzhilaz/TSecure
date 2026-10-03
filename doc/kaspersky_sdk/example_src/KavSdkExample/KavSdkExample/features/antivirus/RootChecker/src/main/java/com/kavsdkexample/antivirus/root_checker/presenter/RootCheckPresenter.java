/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.root_checker.presenter;

import com.kavsdkexample.antivirus.base.presenter.AntivirusBasePresenter;
import com.kavsdkexample.antivirus.root_checker.view.RootCheckView;
import com.kavsdkexample.core.app.view.BaseViewState;

public interface RootCheckPresenter extends AntivirusBasePresenter<RootCheckView, BaseViewState> {
    void checkRoot();
}
