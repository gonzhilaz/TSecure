/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.interactors.impl;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import androidx.annotation.NonNull;

import com.kavsdkexample.core.R;
import com.kavsdkexample.core.app.model.interactors.NotificationInteractor;

public class NotificationInteractorImpl implements NotificationInteractor {
    private static final String NOTIFICATION_CHANNEL_ID    = "KavSdkExample_01";
    private static final int    FOREGROUND_NOTIFICATION_ID = 1000;

    private final Context mContext;

    public NotificationInteractorImpl(@NonNull Context context) {
        mContext = context;
    }

    @Override
    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            CharSequence name           = mContext.getString(R.string.str_notification_channel_name);
            String description          = mContext.getString(R.string.str_notification_channel_desc);
            int importance              = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            //noinspection ConstantConditions
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    @NonNull
    public String getForegroundNotificationChannelId() {
        return NOTIFICATION_CHANNEL_ID;
    }

    @Override
    public int getForegroundNotificationId() {
        return FOREGROUND_NOTIFICATION_ID;
    }
}
