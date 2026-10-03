/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.view.impl;

import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.kavsdkexample.antivirus.base.model.ScanResults;
import com.kavsdkexample.antivirus.base.presenter.ScanResultsPresenter;
import com.kavsdkexample.antivirus.base.presenter.ViewSwitcherPresenter;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatType;
import com.kavsdkexample.antivirus.base.scanner.R;
import com.kavsdkexample.antivirus.base.scanner.R2;
import com.kavsdkexample.antivirus.base.view.ScanResultsView;
import com.kavsdkexample.antivirus.base.view.ViewSwitcherView;
import com.kavsdkexample.core.app.presenter.BasePresenter;
import com.kavsdkexample.core.app.view.BaseView;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.core.app.utils.AndroidUtils;
import com.kavsdkexample.core.ui.BaseFragment;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;

public abstract class OdsScannerMainFragment extends    BaseFragment<ViewSwitcherView,
                                                                     BaseViewState,
                                                                     ViewSwitcherPresenter>
                                             implements ViewSwitcherView {

    @Inject
    ViewSwitcherPresenter mPresenter;
    @BindView(R2.id.fragment_container) FrameLayout mChildFragmentHolder;

    private Unbinder mUnbinder;

    protected abstract Class<? extends AntivirusBaseFragment<? extends BaseView,
                                                             ? extends BaseViewState,
                                                             ? extends BasePresenter>> getScannerFragmentClass();
    protected abstract Class<? extends ScanResultsFragment<? extends ScanResultsView<? extends ScanResults>,
                                                           ? extends BaseViewState,
                                                           ? extends ScanResultsPresenter<
                                                                   ? extends ScanResultsView<? extends ScanResults>,
                                                                   ? extends BaseViewState,
                                                                   ? extends ScanResults>,
                                                           ? extends ScanResults>> getScanResultsFragmentClass();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        processBackPressed();
        View view = inflater.inflate(R.layout.scanner_main_fragment, container, false);
        AndroidSupportInjection.inject(this);
        mUnbinder = ButterKnife.bind(this, view);

        if (getChildFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            BaseFragment fragment = AndroidUtils.newInstance(getScannerFragmentClass());
            getChildFragmentManager().beginTransaction().add(
                R.id.fragment_container,
                fragment,
                fragment.getFragmentTag()
            ).commit();
        }

        return view;
    }

    @Override
    public boolean onBackPressed() {
        boolean handled = super.onBackPressed();
        if (handled) {
            return handled;
        }
        requireFragmentManager().popBackStack();
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    @NonNull
    protected ViewSwitcherPresenter getPresenter() {
        return mPresenter;
    }

    @Nullable
    @Override
    protected BaseViewState getViewState() {
        return null;
    }

    private boolean isHidden(@NonNull Class<? extends Fragment> fragmentClass) {
        //noinspection ConstantConditions
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.fragment_container);
        return fragment == null || !fragment.getClass().equals(fragmentClass);
    }

    @Override
    public void showScanView() {
        Class<? extends BaseFragment> scannerFragmentClass = getScannerFragmentClass();
        if (isHidden(scannerFragmentClass)) {
            BaseFragment scannerFragment = AndroidUtils.newInstance(scannerFragmentClass);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                getChildFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, scannerFragment, scannerFragment.getFragmentTag()).commitNow();
            } else {
                getChildFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, scannerFragment, scannerFragment.getFragmentTag()).commit();
                getChildFragmentManager().executePendingTransactions();
            }
        }
    }

    @Override
    public void showScanResultsView() {
        Class<? extends BaseFragment> scanResultsFragmentClass = getScanResultsFragmentClass();
        if (isHidden(scanResultsFragmentClass)) {
            BaseFragment scanResultsFragment = AndroidUtils.newInstance(scanResultsFragmentClass);
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, scanResultsFragment, scanResultsFragment.getFragmentTag()).commitNow();
        }
    }

    @Override
    public void showThreatsInfoView(@NonNull ThreatType threatType) {
        if (isHidden(LastScanThreatsFragment.class)) {
            getChildFragmentManager().beginTransaction().addToBackStack(null)
                    .replace(R.id.fragment_container, LastScanThreatsFragment.newInstance(threatType)).commit();
        }
    }

    @Override
    public void showApplications(boolean odsApplications) {
        DetectedApplicationsActivity.launch(requireContext(), null, DetectedApplicationsActivity.TabType.Ods);
    }
}
