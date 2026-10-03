/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.easy_scanner.sdk.impl;

import com.kavsdk.antivirus.easyscanner.EasyResult;
import com.kavsdkexample.antivirus.base.model.impl.ScanResultsImpl;
import com.kavsdkexample.antivirus.easy_scanner.model.EasyScanResults;

public class EasyScanResultsImpl extends ScanResultsImpl
                                 implements EasyScanResults {

    private final boolean mRooted;

    EasyScanResultsImpl(
            long       startTime,
            EasyResult easyResult,
            int        threatsDetected,
            int        malwareDetected,
            int        riskwareAdwareDetected,
            int        filesDeleted,
            int        filesQuarantined) {

        super(startTime,
              System.currentTimeMillis(),
              easyResult.getFilesScanned(),
              easyResult.getObjectsScanned(),
              easyResult.getObjectsSkipped(),
              threatsDetected,
              malwareDetected,
              riskwareAdwareDetected,
              filesDeleted,
              filesQuarantined);
        mRooted = easyResult.isRooted();
    }

    @Override
    public boolean isRooted() {
        return mRooted;
    }
}
