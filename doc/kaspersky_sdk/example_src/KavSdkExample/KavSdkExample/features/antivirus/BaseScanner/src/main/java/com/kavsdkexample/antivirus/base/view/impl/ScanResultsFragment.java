/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.view.impl;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.kavsdkexample.antivirus.base.model.ScanResults;
import com.kavsdkexample.antivirus.base.presenter.ScanResultsPresenter;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatType;
import com.kavsdkexample.antivirus.base.scanner.R;
import com.kavsdkexample.antivirus.base.scanner.R2;
import com.kavsdkexample.antivirus.base.view.ScanResultsView;
import com.kavsdkexample.core.app.view.BaseViewState;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public abstract class ScanResultsFragment<VIEW        extends ScanResultsView<SCANRESULTS>,
                                          VIEWSTATE   extends BaseViewState,
                                          PRESENTER   extends ScanResultsPresenter<VIEW, VIEWSTATE, SCANRESULTS>,
                                          SCANRESULTS extends ScanResults>
                             extends    AntivirusBaseFragment<VIEW, VIEWSTATE, PRESENTER>
                             implements ScanResultsView<SCANRESULTS> {

    private Unbinder    mUnbinder;
    private ViewsHolder mViewsHolder;
    private boolean     mScanResultsShown;

    @Override
    @CallSuper
    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.scan_results_fragment, container, false);
        mViewsHolder    = new ViewsHolder(getPresenter());
        mUnbinder       = ButterKnife.bind(mViewsHolder, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
        mUnbinder         = null;
        mViewsHolder      = null;
        mScanResultsShown = false;
    }

    @Nullable
    @Override
    protected VIEWSTATE getViewState() {
        return null;
    }

    @Override
    public boolean onBackPressed() {
        getPresenter().dismiss();
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getPresenter().hasApplicationThreats()) {
            mViewsHolder.mViewApplicationsButton.setVisibility(View.VISIBLE);
        } else {
            mViewsHolder.mViewApplicationsButton.setVisibility(View.GONE);
        }
    }

    @Override
    @CallSuper
    public void showScanResults(@NonNull SCANRESULTS scanResults) {
        long scanDurationMs = scanResults.getScanFinishedTime() - scanResults.getScanStartedTime();
        mViewsHolder.mTimeText.setText(DateUtils.formatElapsedTime(TimeUnit.MILLISECONDS.toSeconds(scanDurationMs)));
        mViewsHolder.mFilesFoundText.setText(String.valueOf(scanResults.getScannedFilesCount()));
        mViewsHolder.mFilesCheckedText.setText(String.valueOf(scanResults.getScannedObjectsCount()));
        mViewsHolder.mFilesSkippedText.setText(String.valueOf(scanResults.getSkippedObjectsCount()));
        mViewsHolder.mThreatsDetectedText.setText(String.valueOf(scanResults.getDetectedThreatsCount()));
        formatTextWithDetails(mViewsHolder.mMalwareDetectedText, scanResults.getCountOfMalwareThreats());
        formatTextWithDetails(mViewsHolder.mRiskwareAdwareDetectedText, scanResults.getCountOfAdwareAndRiskwareThreats());
        mViewsHolder.mFilesDeletedText.setText(String.valueOf(scanResults.getCountOfDeletedFiles()));
        mViewsHolder.mFilesQuarantinedText.setText(String.valueOf(scanResults.getCountOfQuarantinedFiles()));
        mScanResultsShown = true;
    }

    @Override
    public boolean isScanResultsShown() {
        return mScanResultsShown;
    }

    protected void formatTextWithDetails(final TextView tv, int count) {
        if (count > 0) {
            tv.setTextColor(Color.BLUE);
            String text = count + getString(R.string.str_antivirus_files_scan_result_detail);
            tv.setText(text);
        } else {
            tv.setTextColor(Color.BLACK);
            tv.setText(String.valueOf(count));
            tv.setOnClickListener(null);
        }
    }

    static class ViewsHolder implements View.OnClickListener {
        @BindView(R2.id.timeResultCaption)        TextView mTimeText;
        @BindView(R2.id.totalFilesCaption)        TextView mFilesFoundText;
        @BindView(R2.id.filesCheckedCaption)      TextView mFilesCheckedText;
        @BindView(R2.id.filesSkippedCaption)      TextView mFilesSkippedText;
        @BindView(R2.id.totalDetectedCaption)     TextView mThreatsDetectedText;
        @BindView(R2.id.malwareCaption)           TextView mMalwareDetectedText;
        @BindView(R2.id.riskwareAdwareCaption)    TextView mRiskwareAdwareDetectedText;
        @BindView(R2.id.filesDeletedCaption)      TextView mFilesDeletedText;
        @BindView(R2.id.filesQuarantineedCaption) TextView mFilesQuarantinedText;
        @BindView(R2.id.viewApplicationsButton)   Button mViewApplicationsButton;

        private ScanResultsPresenter mPresenter;

        ViewsHolder(@NonNull ScanResultsPresenter presenter) {
            mPresenter = presenter;
        }

        @Override
        @OnClick({ R2.id.checkButton,
                   R2.id.viewApplicationsButton,
                   R2.id.malwareCaption,
                   R2.id.riskwareAdwareCaption,
                   R2.id.filesSuspiciousCaption })
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.checkButton) {
                mPresenter.dismiss();
            } else if (id == R.id.malwareCaption) {
                mPresenter.switchToThreatsInfo(ThreatType.Malware);
            } else if (id == R.id.riskwareAdwareCaption) {
                mPresenter.switchToThreatsInfo(ThreatType.RiskwareAdware);
            } else if (id == R.id.filesSuspiciousCaption) {
                mPresenter.switchToThreatsInfo(ThreatType.Suspicious);
            } else if (id == R.id.viewApplicationsButton) {
                mPresenter.switchToApplicationThreats();
            } else {
                throw new IllegalStateException("Unsupported onclick event: " + id);
            }
        }
    }
}
