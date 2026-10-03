/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.scanner.sdk.impl;

import android.os.Build;
import androidx.annotation.NonNull;
import android.util.Log;

import com.kavsdk.antivirus.ScannerConstants;
import com.kavsdkexample.antivirus.base.model.ScanObjectsType;
import com.kavsdkexample.antivirus.base.model.settings.AvFeatureSettings;
import com.kavsdkexample.antivirus.base.model.settings.BaseOdsSettings;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class ScanUtils {
    private static final String TAG = ScanUtils.class.getSimpleName();

    private ScanUtils() {
    }

    public static int makeScanMode(@NonNull AvFeatureSettings settings) {
        int scanMode = 0;
        if (settings.getScanSuspicious()) {
            scanMode |= ScannerConstants.SCAN_MODE_DETECT_SUSPICIOUS;
        }
        if (settings.getAllowCloudScan()) {
            scanMode |= ScannerConstants.SCAN_MODE_ALLOW_UDS;
        }
        if (settings.getCloudOnlyScan()) {
            scanMode |= ScannerConstants.SCAN_MODE_ONLY_UDS;
        }
        if (settings.getScanRiskware()) {
            scanMode |= ScannerConstants.SCAN_MODE_DETECT_RISKWARE_ADWARE;
        }
        return scanMode;
    }

    public static int makeCleanMode(boolean tryCure) {
        return tryCure ? ScannerConstants.CLEAN_MODE_CLEAN : ScannerConstants.CLEAN_MODE_DONOTCLEAN;
    }

    public static BaseScanTask.ScanResult checkScanType(@NonNull BaseOdsSettings settings) {
        ScanObjectsType scanObjectsType = settings.getObjectsToScanType();
        if (scanObjectsType == ScanObjectsType.File || scanObjectsType == ScanObjectsType.Folder) {
            File pathToScan = new File(settings.getObjectsToScanPath());
            if (!pathToScan.exists()) {
                return BaseScanTask.ScanResult.PathNotExists;
            } else if (scanObjectsType == ScanObjectsType.File && !pathToScan.isFile()) {
                return BaseScanTask.ScanResult.PathNotFile;
            } else if (scanObjectsType == ScanObjectsType.Folder && !pathToScan.isDirectory()) {
                return BaseScanTask.ScanResult.PathNotFolder;
            }
        } else if (scanObjectsType == ScanObjectsType.Document || scanObjectsType == ScanObjectsType.DirectoryDocument) {
            boolean isDirScanType = (scanObjectsType == ScanObjectsType.DirectoryDocument);
            if (!settings.getObjectsToScanPath().startsWith("content://")) {
                return isDirScanType ? BaseScanTask.ScanResult.PathNotFolder : BaseScanTask.ScanResult.PathNotFile;
            }
        }
        return BaseScanTask.ScanResult.Finished;
    }

    @NonNull
    public static String[] getFoldersToScan(
            @NonNull ScanObjectsType scanObjectsType,
            @NonNull String pathToScan,
            @NonNull List<String> excludedFolders) {

        // Use the root directory as a starting one
        List<String> folderNames =  new ArrayList<>();
        folderNames.add("/");

        switch (scanObjectsType) {
            case Document:
            case DirectoryDocument:
            case SdCard:
                folderNames.set(0, pathToScan);
                Log.d(TAG, "Scan started -> SD Card: " + pathToScan);
                break;

            case Folder:
                try {
                    folderNames.set(0, new File(pathToScan).getCanonicalPath());
                } catch (IOException e) {
                    folderNames.set(0, pathToScan);
                }
                Log.d(TAG, "Scan started -> Selected folder: " + pathToScan+ " (" + folderNames.get(0) + ")");
                break;

            case DeviceInternalStorage:
                excludedFolders.addAll(com.kavsdk.utils.Utils.getStoragePaths(Integer.MAX_VALUE));
                Log.d(TAG, "Scan started -> Device Internal Storage");
                break;

            default:
                Log.d(TAG, "Scan started -> AllFiles Files");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ArrayList<String> sdCardPaths = com.kavsdk.utils.Utils.getStoragePaths(Integer.MAX_VALUE);
                    for (final String path : sdCardPaths) {
                        folderNames.add(path);
                    }
                }
                break;
        }
        return folderNames.toArray(new String[0]);
    }
}