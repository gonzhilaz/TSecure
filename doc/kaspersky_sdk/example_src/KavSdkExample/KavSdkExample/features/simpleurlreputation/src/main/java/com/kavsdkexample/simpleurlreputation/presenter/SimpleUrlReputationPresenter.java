/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.simpleurlreputation.presenter;

import com.kavsdkexample.simpleurlreputation.view.SimpleUrlReputationView;
import com.kavsdkexample.simpleurlreputation.view.SimpleUrlReputationViewState;
import com.kavsdkexample.core.app.presenter.BasePresenter;

public interface SimpleUrlReputationPresenter extends BasePresenter<SimpleUrlReputationView, SimpleUrlReputationViewState> {
    void checkUrl(String url);
}
