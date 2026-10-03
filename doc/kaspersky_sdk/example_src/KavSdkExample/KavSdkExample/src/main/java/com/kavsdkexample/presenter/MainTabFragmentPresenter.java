/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.presenter;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.features.TabDescription;
import com.kavsdkexample.core.app.presenter.BasePresenter;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.view.sdk.MainTabFragmentView;

public interface MainTabFragmentPresenter extends BasePresenter<MainTabFragmentView, BaseViewState> {
    void setTabId(@NonNull TabDescription.TabId tabId);
}
