/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.eula.model.tasks.impl;

import android.content.Context;
import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.utils.IoUtils;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.eula.R;
import com.kavsdkexample.eula.model.tasks.EulaLoadObserver;

import java.io.IOException;
import java.io.InputStream;

public class LoadEulaTask implements Runnable {
    private final Context          mContext;
    private final ThreadManager    mThreadManager;
    private final EulaLoadObserver mObserver;

    LoadEulaTask(@NonNull Context          context,
                 @NonNull ThreadManager    threadManager,
                 @NonNull EulaLoadObserver observer) {
        mContext       = context;
        mThreadManager = threadManager;
        mObserver      = observer;
    }

    @Override
    public void run() {
        InputStream inputStream = mContext.getResources().openRawResource(R.raw.eula);
        String tmpEula = "";
        try {
            tmpEula = IoUtils.toString(inputStream);
        } catch (IOException ignored) {
        } finally {
            IoUtils.closeQuietly(inputStream);
        }
        final String eula = tmpEula;
        mThreadManager.runOnUiThread(() -> mObserver.onEulaLoaded(eula));
    }
}