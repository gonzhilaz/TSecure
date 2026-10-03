/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.view.permissions.impl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.kavsdkexample.R;
import com.kavsdkexample.R2;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.core.ui.BaseFragment;
import com.kavsdkexample.presenter.permissions.InsufficientPermissionsPresenter;
import com.kavsdkexample.view.permissions.InsufficientPermissionsView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;

public class InsufficientPermissionsFragment extends    BaseFragment<InsufficientPermissionsView,
                                                                     BaseViewState,
                                                                     InsufficientPermissionsPresenter>
                                             implements InsufficientPermissionsView,
                                                        View.OnClickListener {
    @Inject
    InsufficientPermissionsPresenter mPresenter;

    @BindView(R2.id.permissions_fragment_details)
    TextView mDetails;
    @BindView(R2.id.permissions_fragment_try_again_button)
    Button   mTryAgainButton;

    private Unbinder mUnbinder;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.insufficient_permissions_fragment, container, false);
        AndroidSupportInjection.inject(this);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @NonNull
    @Override
    protected InsufficientPermissionsPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    @Nullable
    protected BaseViewState getViewState() {
        return null;
    }

    @Override
    @OnClick({R2.id.permissions_fragment_try_again_button,
              R2.id.permissions_fragment_proceed_button})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.permissions_fragment_try_again_button) {
            mTryAgainButton.setEnabled(false);
            mPresenter.tryAgain();
        } else if (id == R.id.permissions_fragment_proceed_button) {
            mPresenter.proceed();
        } else {
            throw new IllegalStateException("Unsupported clickable view: " + id);
        }
    }

    @Override
    public void showInsufficientPermissions(@NonNull String permissions) {
        mDetails.setText(permissions);
    }
}
