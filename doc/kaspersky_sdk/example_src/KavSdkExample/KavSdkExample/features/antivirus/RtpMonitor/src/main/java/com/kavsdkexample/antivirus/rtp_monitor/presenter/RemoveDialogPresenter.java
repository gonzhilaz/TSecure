/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.rtp_monitor.presenter;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.presenter.BasePresenter;
import com.kavsdkexample.core.app.view.BaseView;
import com.kavsdkexample.core.app.view.BaseViewState;

public interface RemoveDialogPresenter extends BasePresenter<BaseView, BaseViewState> {
    void removeFolder(@NonNull RemoveType type, @NonNull String folder);
}
