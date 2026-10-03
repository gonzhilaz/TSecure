/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.self_defense.view.impl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.kavsdkexample.core.ui.BaseFragment;
import com.kavsdkexample.self_defense.R;
import com.kavsdkexample.self_defense.R2;
import com.kavsdkexample.self_defense.presenter.CheckSignaturePresenter;
import com.kavsdkexample.self_defense.view.CheckSignatureView;
import com.kavsdkexample.self_defense.view.CheckSignatureViewState;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;

/**
 * This is a sample activity for demonstrating the signature checker functionality.
 * It allows to check the application signature and shows the result
 */
public class CheckSignatureFragment extends    BaseFragment<CheckSignatureView,
                                                            CheckSignatureViewState,
                                                            CheckSignaturePresenter>
                                    implements CheckSignatureView,
                                               OnClickListener {

    @Inject                             CheckSignaturePresenter mPresenter;
    @BindView(R2.id.resultTextView)     TextView                mResultTextView;
    @BindView(R2.id.performCheckButton) Button                  mCheckButton;

    private Unbinder mUnbinder;
    private CheckSignatureViewState mViewState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.check_signature_fragment, container, false);
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
            mViewState = CheckSignatureViewStateHelper.fromBundle(savedInstanceState);
            mResultTextView.setText(mViewState.getCheckResult());
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        CheckSignatureViewStateHelper.toBundle(outState, new CheckSignatureViewStateImpl(mResultTextView.getText().toString()));
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public boolean onBackPressed() {
        mPresenter.switchToSelfDefenseFeatureFragment();
        return true;
    }

    @Override
    @OnClick({ R2.id.performCheckButton })
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.performCheckButton) {
            v.setEnabled(false);
            showProgressDialog();
            mPresenter.checkAppSignature();
        }
    }

    @NonNull
    @Override
    protected CheckSignaturePresenter getPresenter() {
        return mPresenter;
    }

    @Nullable
    @Override
    protected CheckSignatureViewState getViewState() {
        return mViewState;
    }

    @Override
    public void showApplicationCheckResult(@NonNull String result) {
        hideProgressDialog();
        mResultTextView.setText(result);
        mCheckButton.setEnabled(true);
    }
}
