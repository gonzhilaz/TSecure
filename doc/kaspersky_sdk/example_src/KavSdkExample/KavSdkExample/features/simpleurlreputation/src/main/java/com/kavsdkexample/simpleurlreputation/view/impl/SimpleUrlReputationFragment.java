/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.simpleurlreputation.view.impl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.kavsdkexample.simpleurlreputation.R;
import com.kavsdkexample.simpleurlreputation.R2;
import com.kavsdkexample.simpleurlreputation.presenter.SimpleUrlReputationPresenter;
import com.kavsdkexample.simpleurlreputation.view.SimpleUrlReputationView;
import com.kavsdkexample.simpleurlreputation.view.SimpleUrlReputationViewState;
import com.kavsdkexample.core.ui.BaseFragment;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;

public class SimpleUrlReputationFragment
        extends BaseFragment<SimpleUrlReputationView, SimpleUrlReputationViewState, SimpleUrlReputationPresenter>
        implements SimpleUrlReputationView, View.OnClickListener {

    @Inject
    SimpleUrlReputationPresenter mPresenter;

    private SimpleUrlReputationViewState mViewState;
    private Unbinder mUnbinder;

    @BindView(R2.id.simple_url_reputation_edit_text)
    EditText mEditText;

    @BindView(R2.id.simple_url_reputation_text_view)
    TextView mTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.simple_url_reputation_main_fragment, container, false);
        AndroidSupportInjection.inject(this);
        mUnbinder = ButterKnife.bind(this, view);
        mViewState = (savedInstanceState == null) ? null : restoreFromBundle(savedInstanceState);
        return view;
    }

    private static SimpleUrlReputationViewState restoreFromBundle(@NonNull Bundle bundle) {
        return new SimpleUrlReputationViewStateImpl();
    }

    @NonNull
    @Override
    protected SimpleUrlReputationPresenter getPresenter() {
        return mPresenter;
    }

    @Nullable
    @Override
    protected SimpleUrlReputationViewState getViewState() {
        return mViewState;
    }

    @Override
    @OnClick(R2.id.simple_url_reputation_button)
    public void onClick(View v) {
        if (v.getId() == R.id.simple_url_reputation_button) {
            mPresenter.checkUrl(mEditText.getText().toString());
        }
    }

    @Override
    public void onResult(@NonNull String result) {
        mTextView.setText(result);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
