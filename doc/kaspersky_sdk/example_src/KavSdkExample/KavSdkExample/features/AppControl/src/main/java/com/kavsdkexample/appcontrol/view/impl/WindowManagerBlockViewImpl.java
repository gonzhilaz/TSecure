/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.appcontrol.view.impl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewManager;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.kavsdkexample.appcontrol.R;
import com.kavsdkexample.appcontrol.view.AppControlUtils;
import com.kavsdkexample.appcontrol.view.WindowManagerBlockView;

public class WindowManagerBlockViewImpl implements WindowManagerBlockView {
    private static final String TAG = WindowManagerBlockViewImpl.class.getSimpleName();
    private final Context mContext;
    private boolean mBlockingWindowShown;

    public WindowManagerBlockViewImpl(@NonNull final Context context) {
        mContext = context;
    }

    @Override
    public void showDialog() {
        try {
            showWindow((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ?
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        } catch (final Throwable e) {
            final String errorMessage = mContext.getString(
                    R.string.str_app_control_failed_to_show_window_error
            );
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) &&
                    (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)) {
                try {
                    showWindow((Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) ?
                            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY :
                            WindowManager.LayoutParams.TYPE_TOAST);
                    return;
                } catch (final Throwable throwable) {
                    Log.e(TAG, errorMessage, throwable);
                }
            }
            Log.e(TAG, errorMessage, e);
            Toast.makeText(mContext, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    private void showWindow(final int windowType) {
        if (!mBlockingWindowShown) {
            final ViewManager windowManager = (WindowManager) mContext.getSystemService(
                    Context.WINDOW_SERVICE
            );
            final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE
            );
            if (inflater != null) {
                @SuppressLint("InflateParams") final View infoView = inflater.inflate(
                        R.layout.block_app_screen,
                        null
                );
                final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        windowType,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                );

                final Button button = infoView.findViewById(R.id.block_app_button);
                button.setOnClickListener(v -> {
                    AppControlUtils.launchHomeScreen(mContext);
                    if (windowManager != null) {
                        windowManager.removeView(infoView);
                        mBlockingWindowShown = false;
                    }
                });
                infoView.setBackgroundColor(mContext
                        .getResources()
                        .getColor(R.color.appcontrol_block_window_background));
                if (windowManager != null) {
                    windowManager.addView(infoView, layoutParams);
                    mBlockingWindowShown = true;
                }
            }
        }
    }
}
