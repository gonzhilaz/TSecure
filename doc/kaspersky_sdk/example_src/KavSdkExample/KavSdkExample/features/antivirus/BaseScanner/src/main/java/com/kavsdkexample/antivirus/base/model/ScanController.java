/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.model;

import androidx.annotation.UiThread;

@UiThread
public interface ScanController {
    void    stopScan();
    void    pauseScan();
    void    resumeScan();
    boolean isPaused();
    /**
     * Checks if scan task is completed
     * @return true if scan is completed and false otherwise
     */
    boolean isStopped();
}
