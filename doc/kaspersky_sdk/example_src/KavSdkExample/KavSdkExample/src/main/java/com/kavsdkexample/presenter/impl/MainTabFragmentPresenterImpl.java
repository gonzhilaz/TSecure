/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.presenter.impl;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.features.TabDescription;
import com.kavsdkexample.core.app.presenter.impl.BasePresenterImpl;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.model.AppModel;
import com.kavsdkexample.presenter.MainTabFragmentPresenter;
import com.kavsdkexample.view.sdk.MainTabFragmentView;

import javax.inject.Inject;

public class MainTabFragmentPresenterImpl extends    BasePresenterImpl<MainTabFragmentView,
                                                                       BaseViewState,
                                                                       AppModel>
                                          implements MainTabFragmentPresenter {
    private TabDescription.TabId mTabId;

    @Inject
    MainTabFragmentPresenterImpl(@NonNull AppModel model) {
        super(model);
    }

    @Override
    public void onSdkInited() {
        if (isViewCreated()) {
            showFeatures();
        }
    }

    private void showFeatures() {
        if (mView != null) {
            mView.showFeatures(mModel.getFeaturesForTab(mTabId));
        }
    }

    @Override
    public void setTabId(@NonNull TabDescription.TabId tabId) {
        mTabId = tabId;
    }
}
