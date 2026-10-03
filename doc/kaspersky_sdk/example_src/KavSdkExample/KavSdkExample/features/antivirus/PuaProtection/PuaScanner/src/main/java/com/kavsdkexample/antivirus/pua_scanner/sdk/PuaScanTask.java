/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.pua_scanner.sdk;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.kavsdk.antivirus.ThreatInfo;
import com.kavsdk.antivirus.ThreatInfoSerializer;
import com.kavsdk.antivirus.puaprotection.PuaCloudConnectionFailedListener;
import com.kavsdk.antivirus.puaprotection.PuaInfo;
import com.kavsdkexample.antivirus.base.model.AvAction;
import com.kavsdkexample.antivirus.base.model.ScanObserver;
import com.kavsdk.antivirus.AntivirusInstance;
import com.kavsdk.antivirus.puaprotection.puascanner.PuaScanner;
import com.kavsdkexample.antivirus.base.model.ScanResults;
import com.kavsdkexample.antivirus.base.model.ThreatHandler;
import com.kavsdkexample.antivirus.base.model.ThreatInfoWrapper;
import com.kavsdkexample.antivirus.base.model.ThreatInfoWrapperFactory;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatFoundedBy;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatInfoCollector;
import com.kavsdkexample.antivirus.base.scanner.sdk.impl.BaseScanTask;
import com.kavsdkexample.antivirus.pua_scanner.BuildConfig;
import com.kavsdkexample.core.app.utils.ThreadManager;

import java.util.List;

public class PuaScanTask extends BaseScanTask<ScanResults, ScanObserver<ScanResults>> implements PuaCloudConnectionFailedListener {

    private final Context mContext;

    private volatile PuaScanner mPuaScanner;

    private final ThreatInfoCollector      mThreatInfoCollector;
    private final ThreatInfoWrapperFactory mThreatInfoFactory;

    PuaScanTask(@NonNull Context                         context,
                @NonNull ThreatInfoCollector             threatInfoCollector,
                @NonNull ThreatInfoWrapperFactory        threatInfoFactory,
                @NonNull ThreatHandler                   threatHandler,
                @NonNull ThreadManager                   threadManager,
                @NonNull ScanObserver<ScanResults>       scanObserver) {
        super(threatInfoCollector, threatInfoFactory, threatHandler, threadManager, scanObserver);
        mContext             = context;
        mThreatInfoCollector = threatInfoCollector;
        mThreatInfoFactory   = threatInfoFactory;
    }

    @Override
    public void onCloudConnectionFailed(PuaInfo puaInfo) {
        mThreadManager.runOnUiThread(
            () -> Toast.makeText(mContext,
                                 String.format("Can't connect to KSN cloud, app '%s' was not scanned",
                                               puaInfo.getPackageName()),
                                 Toast.LENGTH_LONG).show()
        );
    }

    @Override
    @WorkerThread
    protected ScanResult doRun() {
        if (BuildConfig.DEBUG) {
            mThreadManager.checkWorkerThread();
        }
        if (null == mPuaScanner) {
            mPuaScanner = AntivirusInstance.getInstance().createPuaScanner();
        }

        processPua(mPuaScanner.scan(this));

        return ScanResult.Finished;
    }

    @Override
    protected void doStopScan() {
        final PuaScanner scanner = mPuaScanner;
        if (scanner != null) {
            scanner.stopScan();
        }
    }

    @Override
    protected void doPauseScan() {
        final PuaScanner scanner = mPuaScanner;
        if (scanner != null) {
            scanner.pauseScan();
        }
    }

    @Override
    protected void doResumeScan() {
        final PuaScanner scanner = mPuaScanner;
        if (scanner != null) {
            scanner.resumeScan();
        }
    }

    @Override
    protected ScanResults getResults() {
        return null;
    }

    @Override
    @WorkerThread
    protected void processThreat(@NonNull ThreatInfo threat, @NonNull AvAction action, boolean suspicious) {
        String packageName = threat.getPackageName();
        String category = ((PuaInfo)threat).getPuaCategory().toString();

        ThreatInfoWrapper wrappedThreat = mThreatInfoFactory.createThreatInfo(
                category,
                threat.getFileFullPath(),
                threat.getObjectName(),
                packageName == null ? "" : packageName,
                ThreatInfoSerializer.toBytes(threat),
                threat.getSeverityLevel().getCode(),
                suspicious
        );
        mThreatInfoCollector.addThreat(wrappedThreat, ThreatFoundedBy.Ods);
    }

    private void processPua(List<PuaInfo> puasInfo) {
        for (PuaInfo puaInfo : puasInfo) {
            processThreat(puaInfo, AvAction.SkipThreat, false);
        }
    }
}
