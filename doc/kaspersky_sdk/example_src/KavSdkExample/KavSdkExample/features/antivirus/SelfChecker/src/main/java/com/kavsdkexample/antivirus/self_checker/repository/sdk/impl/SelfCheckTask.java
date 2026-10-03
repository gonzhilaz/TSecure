/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.self_checker.repository.sdk.impl;

import androidx.annotation.NonNull;

import com.kavsdk.antivirus.AntivirusInstance;
import com.kavsdk.antivirus.CompromisedResult;
import com.kavsdk.antivirus.Scanner;
import com.kavsdkexample.antivirus.self_checker.model.SelfCheckResultsObserver;
import com.kavsdkexample.core.app.utils.ThreadManager;

public final class SelfCheckTask implements Runnable {
    private ThreadManager            mThreadManager;
    private SelfCheckResultsObserver mObserver;

    SelfCheckTask(@NonNull ThreadManager threadManager,
                  @NonNull SelfCheckResultsObserver observer) {
        mThreadManager = threadManager;
        mObserver      = observer;
    }

    @Override
    public void run() {
        if (!AntivirusInstance.getInstance().getVirusDbInfo().mAvailable) {
            mThreadManager.runOnUiThread(mObserver::onBasesUnavailable);
            return;
        }

        Scanner scanner = AntivirusInstance.getInstance().createScanner();
        CompromisedResult result = scanner.scanSelf(null);

        mThreadManager.runOnUiThread(() -> mObserver.onSuccess(result.isCompromised()));
    }
}
