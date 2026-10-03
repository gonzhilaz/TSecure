/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.app.permissions;

import androidx.annotation.NonNull;

import java.util.List;

public interface PermissionRepository {
    @NonNull List<String> getRequiredGenericPermissions();
    @NonNull List<String> getRequiredAdditionalPermissions();
             boolean      checkOverlayPermission();
             boolean      checkAllFilesPermission();
             boolean      checkBackgroundLocationPermission();
    @NonNull List<String> getSdCardPathsWithMissingPermissions();
    boolean checkCallScreeningServiceRole();
    boolean checkDefaultDialerStatus();
    boolean checkIgnoreBatterySaving();
    boolean checkNotificationAccessPermission();
    void requestNotificationAccessPermission();
}