/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.kavsdkexample.R;
import com.kavsdkexample.core.app.model.interactors.NotificationInteractor;
import com.kavsdkexample.core.app.model.interactors.ForegroundCaller;
import com.kavsdkexample.core.app.model.interactors.ServiceState;
import com.kavsdkexample.view.impl.MainActivity;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public final class SampleService extends Service {
    private static final String EXTRA_FOREGROUND_CALLER = "foreground_caller";
    private static final String EXTRA_STOP_FOREGROUND   = "stop_foreground";

    @Inject
    NotificationInteractor mNotificationInteractor;

    private final Set<ForegroundCaller> mReferencedCallers = new HashSet<>();

    @Override
    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

   /* public static void startService(Context context) {
        startService(context, Build.VERSION.SDK_INT >= Build.VERSION_CODES.O);
    }*/

    public static void startService(@NonNull Context context,
                                    @NonNull ForegroundCaller caller) {
        Intent serviceIntent = new Intent(context, SampleService.class);
        serviceIntent.putExtra(EXTRA_FOREGROUND_CALLER, caller.ordinal());
        startService(context, serviceIntent, caller);
    }

    public static void stopForegroundService(@NonNull Context context, @NonNull ForegroundCaller caller) {
        Intent serviceIntent = new Intent(context, SampleService.class);
        serviceIntent.putExtra(EXTRA_FOREGROUND_CALLER, caller.ordinal());
        serviceIntent.putExtra(EXTRA_STOP_FOREGROUND, true);
        startService(context, serviceIntent, caller);
    }

    private static void startService(@NonNull Context context, @NonNull Intent serviceIntent, @NonNull ForegroundCaller caller) {
        if (caller != ForegroundCaller.ForegroundNotRequired &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            getServiceState(context) == ServiceState.NotStarted) {

            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    public static ServiceState getServiceState(@NonNull Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //noinspection ConstantConditions
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SampleService.class.getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return ServiceState.Foreground;
                }
                return ServiceState.Background;
            }
        }
        return ServiceState.NotStarted;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidInjection.inject(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int callerId = intent.getIntExtra(EXTRA_FOREGROUND_CALLER, 0);
            ForegroundCaller[] values = ForegroundCaller.values();
            if (callerId >= 0 && callerId < values.length) {
                boolean stopService = intent.getBooleanExtra(EXTRA_STOP_FOREGROUND, false);
                if (stopService) {
                    boolean removed = mReferencedCallers.remove(values[callerId]);
                    if (removed && mReferencedCallers.isEmpty()) {
                        stopForeground(true);
                    }
                } else if (values[callerId] != ForegroundCaller.ForegroundNotRequired) {
                    if (mReferencedCallers.isEmpty()) {
                        showForegroundNotification();
                    }
                    mReferencedCallers.add(values[callerId]);
                }
            }
        }
        return START_STICKY;
    }

    private void showForegroundNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        int piFlags = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            piFlags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, piFlags);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, mNotificationInteractor.getForegroundNotificationChannelId());
        Notification notification = notificationBuilder
                .setSmallIcon(R.drawable.icon)
                .setTicker(this.getText(R.string.str_notification_ticker_text))
                .setContentTitle(this.getText(R.string.str_notification_title))
                .setContentText(this.getText(R.string.str_notification_message))
                .setContentIntent(pendingIntent)
                .build();
        startForeground(mNotificationInteractor.getForegroundNotificationId(), notification);
    }

}
