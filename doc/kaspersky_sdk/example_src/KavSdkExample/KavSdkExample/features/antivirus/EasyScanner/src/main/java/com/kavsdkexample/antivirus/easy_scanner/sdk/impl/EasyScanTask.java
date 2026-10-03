/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.easy_scanner.sdk.impl;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import android.util.Log;

import com.kavsdk.antivirus.AntivirusInstance;
import com.kavsdk.antivirus.ThreatInfo;
import com.kavsdk.antivirus.easyscanner.EasyListener;
import com.kavsdk.antivirus.easyscanner.EasyMode;
import com.kavsdk.antivirus.easyscanner.EasyObject;
import com.kavsdk.antivirus.easyscanner.EasyResult;
import com.kavsdk.antivirus.easyscanner.EasyScanner;
import com.kavsdk.antivirus.easyscanner.EasyStatus;
import com.kavsdkexample.antivirus.base.model.ThreatHandler;
import com.kavsdkexample.antivirus.base.model.ThreatInfoWrapperFactory;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatInfoCollector;
import com.kavsdkexample.antivirus.base.scanner.sdk.impl.BaseScanTask;
import com.kavsdkexample.antivirus.easy_scanner.BuildConfig;
import com.kavsdkexample.antivirus.easy_scanner.model.EasyScanObserver;
import com.kavsdkexample.antivirus.easy_scanner.model.EasyScanResults;
import com.kavsdkexample.antivirus.easy_scanner.model.EasyScannerMode;
import com.kavsdkexample.antivirus.easy_scanner.model.settings.EasyScanSettings;
import com.kavsdkexample.core.app.utils.ThreadManager;

import java.util.List;

public final class EasyScanTask extends    BaseScanTask<EasyScanResults, EasyScanObserver>
                                implements EasyListener {
    private static final String TAG = EasyScanTask.class.getSimpleName();

    private final    EasyScanSettings mSettings;
    private volatile EasyScanner      mScanner;
    private          EasyScanResults  mScanResults;

    EasyScanTask(@NonNull ThreatInfoCollector      threatInfoCollector,
                 @NonNull ThreatInfoWrapperFactory threatInfoFactory,
                 @NonNull ThreatHandler            threatHandler,
                 @NonNull ThreadManager            threadManager,
                 @NonNull EasyScanSettings         settings,
                 @NonNull EasyScanObserver         scanObserver) {
        super(threatInfoCollector, threatInfoFactory, threatHandler, threadManager, scanObserver);
        mSettings = settings;
    }

    @Override
    protected EasyScanResults getResults() {
        return mScanResults;
    }

    @Override
    @WorkerThread
    protected ScanResult doRun() {
        if (BuildConfig.DEBUG) {
            mThreadManager.checkWorkerThread();
        }
        if (mScanner == null) {
            mScanner = AntivirusInstance.getInstance().createEasyScanner();
        }
        EasyScannerMode mode = mSettings.getEasyScannerMode();
        if (!AntivirusInstance.getInstance().getVirusDbInfo().mAvailable &&
             mode != EasyScannerMode.Basic) {
            return ScanResult.BasesUnavailable;
        }
        Log.d(TAG, "Scan started - scan mode: " + mode);

        mScanner.scan(fromEasyScannerMode(mode), this);

        EasyResult easyResult = mScanner.getResult();
        List<ThreatInfo> malwareList = easyResult.getMalwareList();
        List<ThreatInfo> riskwareList = easyResult.getRiskwareList();

        EasyScanResults results = new EasyScanResultsImpl(
            getScanStartedTime(),
            easyResult,
            malwareList.size() + riskwareList.size(),
            malwareList.size(),
            riskwareList.size(),
            mFilesDeleted,
            mFilesQuarantined
        );

        Log.d(TAG, "Scan finished: "        + mode);
        Log.d(TAG, " Files found: "         + results.getScannedFilesCount());
        Log.d(TAG, " Files calculated: "    + easyResult.getFilesCount());
        Log.d(TAG, " Objects scanned: "     + results.getScannedObjectsCount());
        Log.d(TAG, " Objects skipped: "     + results.getSkippedObjectsCount());
        Log.d(TAG, " Threats found: "       + results.getDetectedThreatsCount());
        Log.d(TAG, "       Malware: "       + results.getCountOfMalwareThreats());
        Log.d(TAG, "      Riskware: "       + results.getCountOfAdwareAndRiskwareThreats());
        Log.d(TAG, " Files deleted: "       + results.getCountOfDeletedFiles());
        Log.d(TAG, " Files quarantined: "   + results.getCountOfQuarantinedFiles());
        Log.d(TAG, " Rooted: "              + results.isRooted());

        for (ThreatInfo ti: malwareList) {
            Log.d(TAG, "Malware: " + ti.getVirusName() + ", " + ti.getObjectName());
        }

        for (ThreatInfo ti: riskwareList) {
            Log.d(TAG, "Riskware: " + ti.getVirusName() + ", " + ti.getObjectName());
        }

        mScanResults = results;
        return ScanResult.Finished;
    }

    @Override
    protected void doStopScan() {
        final EasyScanner scanner = mScanner;
        if (scanner != null) {
            scanner.stopScan();
        }
    }

    @Override
    protected void doPauseScan() {
        final EasyScanner scanner = mScanner;
        if (scanner != null) {
            scanner.pauseScan();
        }
    }

    @Override
    protected void doResumeScan() {
        final EasyScanner scanner = mScanner;
        if (scanner != null) {
            scanner.resumeScan();
        }
    }

    @AnyThread
    private static EasyMode fromEasyScannerMode(@NonNull EasyScannerMode mode) {
        switch (mode) {
            case Basic:
                return EasyMode.Basic;
            case Light:
                return EasyMode.Light;
            case LightPlus:
                return EasyMode.LightPlus;
            case Recommended:
                return EasyMode.Recommended;
            case Full:
                return EasyMode.Full;
            default:
                throw new IllegalStateException("Unknown EasyScannerMode: " + mode);
        }
    }

    // EasyListener overrides begin -->
    @Override
    @WorkerThread
    public void onObjectBegin(@NonNull EasyObject object) {
        if (BuildConfig.DEBUG) {
            mThreadManager.checkWorkerThread();
        }
        object.release();
    }

    @Override
    @WorkerThread
    public void onMalwareDetected(@NonNull EasyObject object, @NonNull ThreatInfo threat) {
        if (BuildConfig.DEBUG) {
            mThreadManager.checkWorkerThread();
        }
        Log.e(TAG, "onMalwareDetected: " + threat.getVirusName() + ", " + threat.getFileFullPath() + ", " + threat.getPackageName());
        object.release();

        processThreat(threat, mSettings.getAvAction(), false);
    }

    @Override
    @WorkerThread
    public void onRiskwareDetected(@NonNull EasyObject object, @NonNull ThreatInfo threat) {
        if (BuildConfig.DEBUG) {
            mThreadManager.checkWorkerThread();
        }
        Log.e(TAG, "onRiskwareDetected: " + threat.getVirusName() + ", " + threat.getFileFullPath() + ", " + threat.getPackageName());
        object.release();

        processThreat(threat, mSettings.getAvAction(), false);
    }

    @Override
    @WorkerThread
    public void onRooted() {
        if (BuildConfig.DEBUG) {
            mThreadManager.checkWorkerThread();
        }
        Log.e(TAG, "onRooted");
    }

    @Override
    @WorkerThread
    public void onObjectEnd(@NonNull EasyObject object, @NonNull EasyStatus status) {
        if (BuildConfig.DEBUG) {
            mThreadManager.checkWorkerThread();
        }
        object.release();
    }

    @Override
    public void onFilesCountCalculated(int filesCount) {
        if (BuildConfig.DEBUG) {
            mThreadManager.checkWorkerThread();
        }
        Log.d(TAG, "onFilesCountCalculated: " + filesCount);
    }
    // <-- EasyListener overrides end
}
