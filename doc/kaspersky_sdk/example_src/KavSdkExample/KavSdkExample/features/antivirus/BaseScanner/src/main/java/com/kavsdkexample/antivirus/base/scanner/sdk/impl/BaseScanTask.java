/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.scanner.sdk.impl;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import android.util.Log;

import com.kavsdk.KavSdk;
import com.kavsdk.antivirus.ThreatInfo;
import com.kavsdk.antivirus.ThreatInfoSerializer;
import com.kavsdkexample.antivirus.base.BuildConfig;
import com.kavsdkexample.antivirus.base.model.ScanController;
import com.kavsdkexample.antivirus.base.model.ScanObserver;
import com.kavsdkexample.antivirus.base.model.ScanResults;
import com.kavsdkexample.antivirus.base.model.AvAction;
import com.kavsdkexample.antivirus.base.model.ThreatHandler;
import com.kavsdkexample.antivirus.base.model.ThreatInfoWrapper;
import com.kavsdkexample.antivirus.base.model.ThreatInfoWrapperFactory;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatFoundedBy;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatInfoCollector;
import com.kavsdkexample.core.app.utils.ThreadManager;

import java.io.File;

public abstract class BaseScanTask<SCANRESULTS extends ScanResults, OBSERVER extends ScanObserver<SCANRESULTS>>
                           implements Runnable,
                                      ScanController {
    private static final String TAG = BaseScanTask.class.getSimpleName();

    protected final ThreadManager            mThreadManager;
    private   final OBSERVER                 mScanObserver;
    protected       int                      mFilesDeleted;
    protected       int                      mFilesQuarantined;

    private   final ThreatInfoCollector      mThreatInfoCollector;
    private   final ThreatInfoWrapperFactory mThreatInfoFactory;
    private   final ThreatHandler            mThreatHandler;
    private   final Object                   mLock;
    private         long                     mStartTime;

    private volatile boolean mStopped;
    private volatile boolean mPaused;
    private volatile boolean mWaitingStop;


    public BaseScanTask(@NonNull ThreatInfoCollector      threatInfoCollector,
                        @NonNull ThreatInfoWrapperFactory threatInfoFactory,
                        @NonNull ThreatHandler            threatHandler,
                        @NonNull ThreadManager            threadManager,
                        @NonNull OBSERVER                 scanObserver) {
        mThreatInfoCollector  = threatInfoCollector;
        mThreatInfoFactory    = threatInfoFactory;
        mThreatHandler        = threatHandler;
        mThreadManager        = threadManager;
        mScanObserver         = scanObserver;
        mLock                 = new Object();
        mStopped              = true;
    }

    @Override
    @WorkerThread
    public void run() {
        if (BuildConfig.DEBUG) {
            mThreadManager.checkWorkerThread();
        }

        synchronized (mLock) {
            if (!mStopped || mWaitingStop) {
                return;
            }
            Log.d(TAG, "Starting scan...");
            mStopped   = false;
            mStartTime = System.currentTimeMillis();
        }

        // Please, don't remove this code, otherwise getPathToBases() will be cut in Kashell.
        final File basesPathFile = KavSdk.getPathToBases();
        if (basesPathFile != null) {
            String basesPath = basesPathFile.getAbsolutePath();
            if (basesPath != null) {
                Log.d(TAG, "Path to bases: " + basesPath);
            }
        }

        mWaitingStop = false;
        mStopped = false;
        mThreatInfoCollector.clearThreats(ThreatFoundedBy.Ods);
        mThreadManager.runOnUiThread(()->mScanObserver.onScanStarted(this));

        ScanResult result = doRun();
        switch (result) {
            case BasesUnavailable:
                mThreadManager.runOnUiThread(mScanObserver::onBasesUnavailable);
                break;
            case PathNotExists:
                mThreadManager.runOnUiThread(() -> mScanObserver.onError(ScanObserver.ScanErrorType.PathNotExists));
                break;
            case PathNotFile:
                mThreadManager.runOnUiThread(() -> mScanObserver.onError(ScanObserver.ScanErrorType.PathIsNotFile));
                break;
            case PathNotFolder:
                mThreadManager.runOnUiThread(() -> mScanObserver.onError(ScanObserver.ScanErrorType.PathIsNotFolder));
                break;
            case LicenseError:
                mThreadManager.runOnUiThread(() -> mScanObserver.onError(ScanObserver.ScanErrorType.LicenseError));
                break;
            case Finished:
                mFilesDeleted     = 0;
                mFilesQuarantined = 0;

                mStopped     = true;
                mWaitingStop = false;
                mThreadManager.runOnUiThread(() -> mScanObserver.onScanFinished(getResults()));
                break;
            default:
                throw new IllegalStateException("Unknown scan error");
        }

    }

    @AnyThread
    protected long getScanStartedTime() {
        return mStartTime;
    }

    protected abstract SCANRESULTS getResults();

    @Override
    public void stopScan() {
        Log.d(TAG, "stopScan was called!");
        synchronized (mLock) {
            if (mStopped || mWaitingStop) {
                return;
            }
            Log.d(TAG, "Stopping scan...");
            mWaitingStop = true;
            mStopped     = true;

            doStopScan();

            mPaused = false;
            mLock.notify();
        }
    }

    @Override
    public void pauseScan() {
        Log.d(TAG, "pauseScan was called!");
        synchronized (mLock) {
            if (mStopped || mPaused) {
                return;
            }
            Log.d(TAG, "Pausing scan...");
            mPaused = true;

            doPauseScan();
        }
        mThreadManager.runOnUiThread(mScanObserver::onScanPaused);
    }

    @Override
    public void resumeScan() {
        Log.d(TAG, "resumeScan was called!");
        synchronized (mLock) {
            if (mStopped || !mPaused) {
                return;
            }
            Log.d(TAG, "Resuming scan...");
            doResumeScan();

            mPaused = false;
            mLock.notify();
        }
        mThreadManager.runOnUiThread(mScanObserver::onScanResumed);
    }

    @Override
    public boolean isPaused() {
        return mPaused;
    }

    @Override
    @AnyThread
    public boolean isStopped() {
        return mStopped;
    }

    @AnyThread
    protected void waitNotPaused() {
        synchronized (mLock) {
            while (mPaused) {
                try {
                    mLock.wait();
                } catch (InterruptedException e) {
                    // not used
                }
            }
        }
    }

    @WorkerThread
    protected abstract ScanResult doRun();

    protected abstract void doStopScan();
    protected abstract void doPauseScan();
    protected abstract void doResumeScan();

    @WorkerThread
    protected void processThreat(@NonNull ThreatInfo threat, @NonNull AvAction action, boolean suspicious) {
        String packageName = threat.getPackageName();
        ThreatInfoWrapper wrappedThreat = mThreatInfoFactory.createThreatInfo(
                threat.getVirusName(),
                threat.getFileFullPath(),
                threat.getObjectName(),
                packageName == null ? "" : packageName,
                ThreatInfoSerializer.toBytes(threat),
                threat.getSeverityLevel().getCode(),
                suspicious
        );
        mThreatInfoCollector.addThreat(wrappedThreat, ThreatFoundedBy.Ods);

        if (!threat.isApplication()) {
            switch (action) {
                case DeteteThreat:
                    if (mThreatHandler.removeThreat(wrappedThreat)) {
                        mFilesDeleted++;
                    }
                    break;
                case QuarantineThreat:
                    if (mThreatHandler.quarantineThreat(wrappedThreat)) {
                        mFilesQuarantined++;
                    }
                    break;
                case SkipThreat:
                    break;
                default:
                    throw new IllegalStateException("Unknown scan action: " + action);
            }
        }
    }

    public enum ScanResult {
        Finished,
        BasesUnavailable,
        PathNotExists,
        PathNotFile,
        PathNotFolder,
        LicenseError
    }
}
