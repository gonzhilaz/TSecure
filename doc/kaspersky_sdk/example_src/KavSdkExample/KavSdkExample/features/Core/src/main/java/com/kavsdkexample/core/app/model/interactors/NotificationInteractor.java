/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.model.interactors;

import androidx.annotation.NonNull;

public interface NotificationInteractor {
    void   createNotificationChannel();
    @NonNull
    String getForegroundNotificationChannelId();
    int    getForegroundNotificationId();
}
