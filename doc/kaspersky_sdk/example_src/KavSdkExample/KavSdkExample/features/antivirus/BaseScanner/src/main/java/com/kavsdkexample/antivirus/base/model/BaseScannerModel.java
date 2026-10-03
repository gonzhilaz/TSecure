/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.model;

import androidx.annotation.Nullable;

public interface BaseScannerModel<Y extends ScanResults>
                            extends AntivirusModel,
                                    ViewSwitchModel {
    /**
     * Tries to start on demand scan
     * @return true if scan was started or false otherwise
     */
    boolean startScan();
    void    stopScan();
    void    pauseScan();
    void    resumeScan();
    boolean isScanRunning();
    boolean isScanPaused();

    @Nullable
    Y getScanResults();
    void consumeScanResults();
}
