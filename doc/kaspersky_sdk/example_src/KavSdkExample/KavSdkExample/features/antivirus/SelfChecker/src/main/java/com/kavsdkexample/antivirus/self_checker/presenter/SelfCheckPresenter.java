/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.self_checker.presenter;

import com.kavsdkexample.antivirus.base.presenter.AntivirusBasePresenter;
import com.kavsdkexample.antivirus.self_checker.view.SelfCheckView;
import com.kavsdkexample.core.app.view.BaseViewState;

public interface SelfCheckPresenter extends AntivirusBasePresenter<SelfCheckView, BaseViewState> {
    void checkSelf();
}
