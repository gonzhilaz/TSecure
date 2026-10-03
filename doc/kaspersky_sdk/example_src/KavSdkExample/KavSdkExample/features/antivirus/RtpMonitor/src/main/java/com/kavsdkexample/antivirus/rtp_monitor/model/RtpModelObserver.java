/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.rtp_monitor.model;

import androidx.annotation.NonNull;

import java.util.Map;

public interface RtpModelObserver {
    void onMonitorFolderAdded(@NonNull String folder);
    void onMonitorFolderRemoved(@NonNull String folder);
    void onMonitorFoldersChanged(@NonNull Map<String, Integer> items);

    void onExclusionFolderAdded(@NonNull String folder);
    void onExclusionRemoved(@NonNull String removed);
}
