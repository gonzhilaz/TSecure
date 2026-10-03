/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.presenter.permissions;

import com.kavsdkexample.core.app.presenter.BasePresenter;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.view.permissions.InsufficientPermissionsView;

public interface InsufficientPermissionsPresenter extends BasePresenter<InsufficientPermissionsView, BaseViewState> {
    void tryAgain();
    void proceed();
}
