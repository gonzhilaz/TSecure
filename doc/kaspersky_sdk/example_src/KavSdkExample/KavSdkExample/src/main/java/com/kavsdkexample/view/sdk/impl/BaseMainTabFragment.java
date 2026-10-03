/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.view.sdk.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kavsdkexample.core.app.features.SdkFeature;
import com.kavsdkexample.core.app.features.SdkFeatureAndroid;
import com.kavsdkexample.core.app.features.TabDescription;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.core.ui.BaseFragment;
import com.kavsdkexample.presenter.MainTabFragmentPresenter;
import com.kavsdkexample.view.sdk.MainTabFragmentView;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;


public abstract class BaseMainTabFragment extends     BaseFragment<MainTabFragmentView,
                                                                   BaseViewState,
                                                                   MainTabFragmentPresenter>
                                          implements MainTabFragmentView {
    @Inject
    MainTabFragmentPresenter mPresenter;

    protected ViewGroup mRootViewGroup;
    private Unbinder  mUnbinder;

    @NonNull
    public View initView(@NonNull LayoutInflater inflater,
                         @Nullable ViewGroup container,
                         @NonNull TabDescription.TabId tabId,
                         int fragment) {
        final ViewGroup view = mRootViewGroup = (ViewGroup) inflater.inflate(fragment, container, false);
        AndroidSupportInjection.inject(this);
        mPresenter.setTabId(tabId);
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
    protected MainTabFragmentPresenter getPresenter() {
        return mPresenter;
    }

    @Nullable
    @Override
    protected BaseViewState getViewState() {
        return null;
    }

    protected void showFeatures(@NonNull List<SdkFeature> features, int layoutId) {
        FragmentActivity activity = requireActivity();
        for (SdkFeature feature : features) {
            if (feature instanceof SdkFeatureAndroid) {
                ((SdkFeatureAndroid) feature).initUiView(activity, mRootViewGroup.findViewById(layoutId));
            }
        }
    }
}
