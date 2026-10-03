/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.pua_scanner.sdk;

import android.content.Context;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.model.ScanObserver;
import com.kavsdkexample.antivirus.base.model.ScanResults;
import com.kavsdkexample.antivirus.base.model.ThreatHandler;
import com.kavsdkexample.antivirus.base.model.ThreatInfoWrapperFactory;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatInfoCollector;
import com.kavsdkexample.antivirus.pua_scanner.model.tasks.TasksFactory;
import com.kavsdkexample.core.app.utils.ThreadManager;

public class TasksFactoryImpl extends    com.kavsdkexample.antivirus.base.sdk.impl.TasksFactoryImpl
                              implements TasksFactory {


    public TasksFactoryImpl(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    public Runnable createPuaScanTask(@NonNull ThreatInfoCollector       threatInfoCollector,
                                      @NonNull ThreatInfoWrapperFactory  threatInfoFactory,
                                      @NonNull ThreatHandler             threatHandler,
                                      @NonNull ThreadManager             threadManager,
                                      @NonNull ScanObserver<ScanResults> scanObserver) {
        return new PuaScanTask(mContext,
                threatInfoCollector,
                threatInfoFactory,
                threatHandler,
                threadManager,
                scanObserver);
    }
}
