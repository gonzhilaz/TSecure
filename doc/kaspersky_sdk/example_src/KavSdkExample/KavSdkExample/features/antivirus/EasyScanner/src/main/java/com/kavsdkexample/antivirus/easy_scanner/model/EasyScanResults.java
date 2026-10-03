/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.easy_scanner.model;

import com.kavsdkexample.antivirus.base.model.ScanResults;

public interface EasyScanResults extends ScanResults {
    /**
     * Get if device rooted or not.
     * @return true if device is rooted and false if device is not rooted or easy scan mode
     * that does not check root was selected
     */
    boolean isRooted();
}
