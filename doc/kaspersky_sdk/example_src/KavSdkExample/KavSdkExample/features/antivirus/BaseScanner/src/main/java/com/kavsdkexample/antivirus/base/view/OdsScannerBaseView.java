/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.view;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.model.ScanObjectsType;
import com.kavsdkexample.antivirus.base.model.ScanResults;

import java.util.List;

public interface OdsScannerBaseView<SCANRESULTS extends ScanResults> extends ScannerBaseView<SCANRESULTS> {
    void showSdCardsOptions(@NonNull List<String> sdCardPaths);
    void setObjectsToScan(@NonNull ScanObjectsType scanObjectsType, @NonNull String path);
    void setCloudOnlyScan(boolean enabled);
    void setAllowCloudScan(boolean enabled);
    void setScanSuspicious(boolean enabled);
    void setDetectRiskwareAdware(boolean enabled);
    void selectApplicationToScan();
    void selectFileToScan(@NonNull String currentFile);
    void selectFolderToScan(@NonNull String currentFolder);
    void selectDocumentToScan(@NonNull String currentDocument);
    void selectDirectoryDocumentToScan(@NonNull String currentDirectoryDocument);
}
