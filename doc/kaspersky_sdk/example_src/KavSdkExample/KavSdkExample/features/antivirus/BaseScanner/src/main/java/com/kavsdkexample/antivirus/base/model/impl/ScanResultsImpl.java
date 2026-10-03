/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.model.impl;

import android.util.Log;

import com.kavsdkexample.antivirus.base.model.ScanResults;

public class ScanResultsImpl implements ScanResults {
    private static final String TAG = ScanResultsImpl.class.getSimpleName();

    private final long mStartTime;
    private final long mFinishTime;
    private final int  mFilesCount;
    private final int  mScannedObjects;
    private final int  mSkippedObjects;
    private final int  mThreatsDetected;
    private final int  mMalwareDetected;
    private final int  mRiskwareAdwareDetected;
    private final int  mFilesDeleted;
    private final int  mFilesQuarantined;

    public ScanResultsImpl(long startTime,
                           long finishTime,
                           int  filesCount,
                           int  scannedObjects,
                           int  filesSkipped,
                           int  threatsDetected,
                           int  malwareDetected,
                           int  riskwareAdwareDetected,
                           int  filesDeleted,
                           int  filesQuarantined) {

        mStartTime              = startTime;
        mFinishTime             = finishTime;
        mFilesCount             = filesCount;
        mScannedObjects         = scannedObjects;
        mSkippedObjects         = filesSkipped;
        mThreatsDetected        = threatsDetected;
        mMalwareDetected        = malwareDetected;
        mRiskwareAdwareDetected = riskwareAdwareDetected;
        mFilesDeleted           = filesDeleted;
        mFilesQuarantined       = filesQuarantined;

        Log.d(TAG, "Scan finished");
        Log.d(TAG, " Files found: "         + mFilesCount);
        Log.d(TAG, " Objects scanned: "     + mScannedObjects);
        Log.d(TAG, " Objects skipped: "     + mSkippedObjects);
        Log.d(TAG, " Threats found: "       + mThreatsDetected);
        Log.d(TAG, "       Malware: "       + mMalwareDetected);
        Log.d(TAG, "      Riskware: "       + mRiskwareAdwareDetected);

        Log.d(TAG, " Files deleted: "       + mFilesDeleted);
        Log.d(TAG, " Files quarantineed: "  + mFilesQuarantined);
    }

    @Override
    public long getScanStartedTime() {
        return mStartTime;
    }

    @Override
    public long getScanFinishedTime() {
        return mFinishTime;
    }

    @Override
    public int  getScannedFilesCount() {
        return mFilesCount;
    }

    @Override
    public int  getScannedObjectsCount() {
        return mScannedObjects;
    }

    @Override
    public int  getSkippedObjectsCount() {
        return mSkippedObjects;
    }

    @Override
    public int  getDetectedThreatsCount() {
        return mThreatsDetected;
    }

    @Override
    public int  getCountOfMalwareThreats() {
        return mMalwareDetected;
    }

    @Override
    public int  getCountOfAdwareAndRiskwareThreats() {
        return mRiskwareAdwareDetected;
    }

    @Override
    public int  getCountOfDeletedFiles() {
        return mFilesDeleted;
    }

    @Override
    public int  getCountOfQuarantinedFiles() {
        return mFilesQuarantined;
    }
}
