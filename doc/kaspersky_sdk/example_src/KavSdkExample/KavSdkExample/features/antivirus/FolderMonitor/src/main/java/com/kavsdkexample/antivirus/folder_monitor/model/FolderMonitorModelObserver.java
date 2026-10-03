/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.folder_monitor.model;

import androidx.annotation.NonNull;

public interface FolderMonitorModelObserver {
    void onMonitorFolderAdded(@NonNull String folder);
    void onMonitorFolderRemoved(@NonNull String folder);
    void onMonitorFolderCleared();
}
