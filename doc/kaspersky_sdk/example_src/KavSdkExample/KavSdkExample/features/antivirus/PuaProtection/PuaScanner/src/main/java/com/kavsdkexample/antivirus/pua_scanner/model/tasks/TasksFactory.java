/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.pua_scanner.model.tasks;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.model.ScanObserver;
import com.kavsdkexample.antivirus.base.model.ScanResults;
import com.kavsdkexample.antivirus.base.model.ThreatHandler;
import com.kavsdkexample.antivirus.base.model.ThreatInfoWrapperFactory;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatInfoCollector;
import com.kavsdkexample.core.app.utils.ThreadManager;

public interface TasksFactory extends com.kavsdkexample.antivirus.base.model.tasks.TasksFactory {
    @NonNull
    Runnable createPuaScanTask(@NonNull ThreatInfoCollector       threatInfoCollector,
                               @NonNull ThreatInfoWrapperFactory  threatInfoFactory,
                               @NonNull ThreatHandler             threatHandler,
                               @NonNull ThreadManager             threadManager,
                               @NonNull ScanObserver<ScanResults> scanObserver);
}
