/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.self_defense.view.impl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import com.kavsdkexample.core.ui.BaseFragment;
import com.kavsdkexample.self_defense.R;
import com.kavsdkexample.self_defense.R2;
import com.kavsdkexample.self_defense.presenter.SelfDefenseMainPresenter;
import com.kavsdkexample.self_defense.view.SelfDefenseMainView;
import com.kavsdkexample.self_defense.view.SelfDefenseMainViewState;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;

public class SelfDefenseMainFragment extends    BaseFragment<SelfDefenseMainView,
                                                             SelfDefenseMainViewState,
                                                             SelfDefenseMainPresenter>
                                     implements SelfDefenseMainView {

    private static final int FEATURE_FRAGMENT_POSITION   = 0;
    private static final int SIGNATURE_FRAGMENT_POSITION = 1;

    @Inject                           SelfDefenseMainPresenter mPresenter;
    @BindView(R2.id.fragment_flipper) ViewFlipper              mFragmentFlipper;

    private Unbinder                 mUnbinder;
    private SelfDefenseMainViewState mViewState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        processBackPressed();
        View view = inflater.inflate(R.layout.self_defense_main_fragment, container, false);
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
            mViewState = SelfDefenseMainViewStateHelper.fromBundle(savedInstanceState);
            mFragmentFlipper.setDisplayedChild(mViewState.getDisplayedChildIndex());
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        SelfDefenseMainViewStateHelper.toBundle(outState, new SelfDefenseMainViewStateImpl(mFragmentFlipper.getDisplayedChild()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    @NonNull
    protected SelfDefenseMainPresenter getPresenter() {
        return mPresenter;
    }

    @Nullable
    @Override
    protected SelfDefenseMainViewState getViewState() {
        return mViewState;
    }

    @Override
    public void showFeatureFragment() {
        mFragmentFlipper.setDisplayedChild(FEATURE_FRAGMENT_POSITION);
    }

    @Override
    public void showCheckSignatureFragment() {
        mFragmentFlipper.setDisplayedChild(SIGNATURE_FRAGMENT_POSITION);
    }
}
