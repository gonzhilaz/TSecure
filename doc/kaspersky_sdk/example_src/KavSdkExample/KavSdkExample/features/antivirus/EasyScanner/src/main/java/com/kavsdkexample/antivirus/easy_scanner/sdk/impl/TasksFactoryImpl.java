/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.easy_scanner.sdk.impl;

import android.content.Context;
import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.model.ThreatHandler;
import com.kavsdkexample.antivirus.base.model.ThreatInfoWrapperFactory;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatInfoCollector;
import com.kavsdkexample.antivirus.easy_scanner.model.EasyScanObserver;
import com.kavsdkexample.antivirus.easy_scanner.model.settings.EasyScanSettings;
import com.kavsdkexample.antivirus.easy_scanner.model.tasks.TasksFactory;
import com.kavsdkexample.core.app.utils.ThreadManager;

public final class TasksFactoryImpl extends com.kavsdkexample.antivirus.base.sdk.impl.TasksFactoryImpl implements TasksFactory {
    public TasksFactoryImpl(@NonNull Context context) {
        super(context);
    }

    @Override
    public Runnable createEasyScanTask(@NonNull ThreatInfoCollector      threatInfoCollector,
                                       @NonNull ThreatInfoWrapperFactory threatInfoFactory,
                                       @NonNull ThreatHandler            threatHandler,
                                       @NonNull ThreadManager            threadManager,
                                       @NonNull EasyScanSettings         settings,
                                       @NonNull EasyScanObserver         scanObserver) {
        return new EasyScanTask(threatInfoCollector,
                                threatInfoFactory,
                                threatHandler,
                                threadManager,
                                settings,
                                scanObserver);
    }
}
