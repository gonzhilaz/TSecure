/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.app.sdk.license;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface SdkLicenseInfo {
    /**
     * Check if there was an error during initial sdk activation request.
     *
     * @return true if initial activation request was unsuccessful for some reason, or false otherwise
     */
    boolean isInitialActivationFailed();

    /**
     * Check if activation request was failed due to some troubles on server side (for example if incorrect activation code was supplied)
     * @return true for server side activation error or false otherwise
     */
    boolean isServerSideActivationError();

    /**
     * Check if user id if required (should be used for per user licensing scheme only)
     * @return true if application should provide user id, or false otherwise
     */
    boolean isClientUserIdRequired();

    /**
     * Check if sdk initialization and activation request were successful or not
     * @return null if sdk initialization and activation request were successful or string with error message otherwise
     */
    @Nullable
    String  getErrorMessage();

    /**
     * Check if SDK license are valid or not
     * @return true if license is valid or false otherwise
     */
    boolean isValid();

    /**
     * Get SDK license expiration date
     * @return expiration date for valid license or 0 otherwise
     */
    long    getExpirationDate();

    /**
     * Get hash of hardware id for current device
     * @return hash of hardware id
     */
    @NonNull
    String  getHashOfHardwareId();

    /**
     * Get installation id of current app installation
     * @return installation id
     */
    @NonNull
    String  getInstallationId();
}
