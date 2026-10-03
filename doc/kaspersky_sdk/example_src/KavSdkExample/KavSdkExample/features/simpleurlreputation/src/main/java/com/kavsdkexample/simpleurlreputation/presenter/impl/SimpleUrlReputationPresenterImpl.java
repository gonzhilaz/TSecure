/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.simpleurlreputation.presenter.impl;

import com.kavsdkexample.simpleurlreputation.model.UrlReputationListener;
import com.kavsdkexample.simpleurlreputation.model.SimpleUrlReputationModel;
import com.kavsdkexample.simpleurlreputation.presenter.SimpleUrlReputationPresenter;
import com.kavsdkexample.simpleurlreputation.view.SimpleUrlReputationView;
import com.kavsdkexample.simpleurlreputation.view.SimpleUrlReputationViewState;
import com.kavsdkexample.core.app.presenter.impl.BasePresenterImpl;
import com.kaspersky.components.urlchecker.UrlInfo;

import java.util.Arrays;

import javax.inject.Inject;

public class SimpleUrlReputationPresenterImpl
        extends BasePresenterImpl<SimpleUrlReputationView, SimpleUrlReputationViewState, SimpleUrlReputationModel>
        implements SimpleUrlReputationPresenter {

    @Inject
    SimpleUrlReputationPresenterImpl(SimpleUrlReputationModel model) {
        super(model);
    }

    @Override
    public void checkUrl(String url) {
        mModel.checkUrl(url, new UrlReputationListener() {
            @Override
            public void onSuccess(UrlInfo result) {
                requireView().onResult("Verdict: " + verdictToString(result.mVerdict) + "; Phishing: " + result.isPhishing() + "; Malware: " + result.isMalware() + "; Categories: " + Arrays.toString(result.mCategoriesExt));
            }

            private String verdictToString(int verdict) {
                switch (verdict) {
                    case UrlInfo.VERDICT_UNKNOWN: return "Unknown";
                    case UrlInfo.VERDICT_GOOD: return "Good";
                    case UrlInfo.VERDICT_BAD: return "Bad";
                    default: throw new IllegalArgumentException("Wrong verdict: " + verdict);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                requireView().onResult(errorMessage);
            }
        });
    }
}
