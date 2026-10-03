/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.view.sdk.impl;

import com.kavsdkexample.R;
import com.kavsdkexample.core.ui.BaseActivity;
import com.kavsdkexample.model.app.sdk.SdkWrappersFactory;
import com.kavsdkexample.presenter.proxy.ProxyAuthPresenter;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;

public class ProxyAuthActivity extends BaseActivity<ProxyAuthView, ProxyAuthViewState, ProxyAuthPresenter>
                          implements ProxyAuthView {
    private boolean mGotAuth;

    @Inject
    SdkWrappersFactory mSdkWrappersFactory;

    @Inject
    ProxyAuthPresenter mPresenter;
    private ProxyAuthViewState mViewState;

    @BindView(R.id.checkButton)
    Button mCheckButton;

    @BindView(R.id.loginEditText)
    TextView mTextViewLogin;

    @BindView(R.id.passwordEditText)
    TextView mTextViewPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.proxy_data_dialog);
        AndroidInjection.inject(this);
        ButterKnife.bind(this);

        if (savedInstanceState == null) {
            mViewState = null;
        } else {
            mViewState = new ProxyAuthViewStateImpl();
        }

        mCheckButton.setOnClickListener(v -> {
            mPresenter.setProxyAuthCredentials(
                    mTextViewLogin.getText().toString(),
                    mTextViewPassword.getText().toString());
            mGotAuth = true;
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        if (isFinishing() && !mGotAuth) {
            mPresenter.cancelProxyAuth();
        }
        super.onDestroy();
    }

    @Override
    protected ProxyAuthPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected ProxyAuthViewState getViewState() {
        return mViewState;
    }
}
