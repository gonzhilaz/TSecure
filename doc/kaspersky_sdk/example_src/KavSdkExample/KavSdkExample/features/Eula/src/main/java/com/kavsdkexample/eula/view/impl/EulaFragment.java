/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.eula.view.impl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.kavsdkexample.core.ui.BaseFragment;
import com.kavsdkexample.eula.R;
import com.kavsdkexample.eula.R2;
import com.kavsdkexample.eula.presenter.EulaPresenter;
import com.kavsdkexample.eula.view.EulaView;
import com.kavsdkexample.eula.view.EulaViewState;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;

/**
 * This is a fragment for accepting eula
 */
public class EulaFragment extends    BaseFragment<EulaView, EulaViewState, EulaPresenter>
                          implements EulaView,
                                     OnClickListener {

    @BindView(R2.id.eulaText) WebView       mEulaWebView;
    @Inject                   EulaPresenter mPresenter;

    private Unbinder      mUnbinder;
    private EulaViewState mViewState;
    private boolean       mEulaShown;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.eula_fragment, container, false);
        AndroidSupportInjection.inject(this);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            mViewState = null;
        } else {
            mEulaWebView.restoreState(savedInstanceState);
            mViewState = EulaViewHelper.fromBundle(savedInstanceState);
            mEulaShown = mViewState.isEulaShown();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mEulaWebView.saveState(outState);
        EulaViewHelper.toBundle(outState, new EulaViewStateImpl(mEulaShown));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    @OnClick({R2.id.buttonAcceptEula,
              R2.id.buttonDeclineEula})
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.buttonAcceptEula) {
            mPresenter.acceptEula();
        } else if (i == R.id.buttonDeclineEula) {
            mPresenter.declineEula();
        }
    }

    @Override
    @NonNull
    protected EulaPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    @Nullable
    protected EulaViewState getViewState() {
        return mViewState;
    }

    @Override
    public void showEula(String eula) {
        mEulaWebView.loadData(eula, "text/html", "utf-8");
        mEulaShown = true;
    }
}
