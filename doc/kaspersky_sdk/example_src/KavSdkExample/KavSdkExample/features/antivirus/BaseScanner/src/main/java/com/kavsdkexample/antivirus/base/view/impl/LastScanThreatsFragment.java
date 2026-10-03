/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.view.impl;

import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.kavsdkexample.antivirus.base.model.ThreatInfoWrapperFactory;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatFoundedBy;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatType;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatInfoProvider;
import com.kavsdkexample.antivirus.base.scanner.R;
import com.kavsdkexample.antivirus.base.scanner.R2;
import com.kavsdkexample.core.app.presenter.EmptyPresenter;
import com.kavsdkexample.core.app.view.BaseView;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.core.ui.BaseFragment;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;

public final class LastScanThreatsFragment extends BaseFragment<BaseView,
                                                                BaseViewState,
                                                                EmptyPresenter> {
    private static final String EXTRA_THREAT_TYPE    = "threat_type";

    @Inject ThreatInfoProvider       mThreatInfoProvider;
    @Inject ThreatInfoWrapperFactory mThreatInfoWrapperFactory;
    @Inject EmptyPresenter           mEmptyPresenter;

    @BindView(R2.id.threatsListView) ListView mListView;

    private ListAdapter mAdapter;
    private Unbinder    mUnbinder;

    public static LastScanThreatsFragment newInstance(@NonNull ThreatType threatType) {
        LastScanThreatsFragment fragment = new LastScanThreatsFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(EXTRA_THREAT_TYPE, threatType.ordinal());
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public boolean onBackPressed() {
        requireFragmentManager().popBackStack();
        return true;
    }

    @NonNull
    @Override
    protected EmptyPresenter getPresenter() {
        return mEmptyPresenter;
    }

    @Nullable
    @Override
    protected BaseViewState getViewState() {
        return null;
    }

    @Override
    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        final View view = inflater.inflate(R.layout.last_scan_threats_fragment, container, false);
        mUnbinder       = ButterKnife.bind(this, view);

        Bundle bundle   = getArguments();
        if (bundle == null) {
            throw new IllegalStateException(getClass().getName() + " launched without arguments");
        }
        ThreatType[] values = ThreatType.values();
        int intThreatType   = bundle.getInt(EXTRA_THREAT_TYPE, 0);
        if (intThreatType < 0 || intThreatType >= values.length) {
            throw new IllegalStateException("Unsupported threat id: " + intThreatType);
        }
        ThreatType threatType = values[intThreatType];

        Cursor cursor;
        switch (threatType) {
            case Malware:
                cursor = mThreatInfoProvider.getThreats(ThreatType.Malware, ThreatFoundedBy.Ods);
                break;
            case RiskwareAdware:
                cursor = mThreatInfoProvider.getThreats(ThreatType.RiskwareAdware, ThreatFoundedBy.Ods);
                break;
            case Suspicious:
                cursor = mThreatInfoProvider.getThreats(ThreatType.Suspicious, ThreatFoundedBy.Ods);
                break;
            default:
                throw new IllegalStateException("Unsupported threat type: " + threatType);
        }

        mAdapter = (cursor != null) ?
                new LastScanThreatsAdapter(
                        requireContext().getApplicationContext(),
                        cursor,
                        mThreatInfoProvider,
                        mThreatInfoWrapperFactory) :
                new LastScanThreatsErrorAdapter(requireContext().getApplicationContext());
        mListView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
        mThreatInfoProvider = null;
        if (mAdapter instanceof LastScanThreatsAdapter) {
            ((LastScanThreatsAdapter)mAdapter).closeCursor();
        }
    }
}
