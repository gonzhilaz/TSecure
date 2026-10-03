/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.self_defense.model.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface SdkManager {
    /**
     * Enables or disables sdk to be auto restarted
     * @param value true to enable, false to disable
     */
    void    enableAutoRestart(boolean value);

    boolean isAutoRestartEnabled();
    boolean isNotificationAccessEnabled();
    void    setNotificationAccessStateObserver(@Nullable NotificationStateObserver observer);

    void    checkAppSignature(@NonNull String certFileName, @NonNull AppSignatureCheckObserver observer);

    interface AppSignatureCheckObserver {
        void onAppSignatureCheckResult(@NonNull String result);
    }
}
