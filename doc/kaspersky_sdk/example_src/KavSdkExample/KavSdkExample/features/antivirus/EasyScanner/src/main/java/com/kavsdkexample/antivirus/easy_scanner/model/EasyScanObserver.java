/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.easy_scanner.model;

import androidx.annotation.UiThread;

import com.kavsdkexample.antivirus.base.model.ScanObserver;

@UiThread
public interface EasyScanObserver extends ScanObserver<EasyScanResults> {
}
