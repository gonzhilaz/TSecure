/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.model.impl;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.antivirus.base.model.BaseScannerModel;
import com.kavsdkexample.antivirus.base.model.BaseScannerModelObserver;
import com.kavsdkexample.antivirus.base.model.ScanController;
import com.kavsdkexample.antivirus.base.model.ScanObserver;
import com.kavsdkexample.antivirus.base.model.ScanResults;
import com.kavsdkexample.antivirus.base.model.ThreatHandler;
import com.kavsdkexample.antivirus.base.model.ThreatInfoWrapperFactory;
import com.kavsdkexample.antivirus.base.model.ViewSwitchModelObserver;
import com.kavsdkexample.antivirus.base.model.settings.AvFeatureSettings;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatInfoCollector;
import com.kavsdkexample.antivirus.base.model.tasks.TasksFactory;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatType;
import com.kavsdkexample.antivirus.base.repository.system.PackageMonitor;
import com.kavsdkexample.antivirus.base.sdk.SdkManager;
import com.kavsdkexample.antivirus.base.view.UiLauncher;
import com.kavsdkexample.core.app.utils.ThreadManager;

import java.util.concurrent.ExecutorService;

public abstract class BaseScannerModelImpl<TASKFACTORY extends TasksFactory,
                                           SCANRESULTS extends ScanResults,
                                           SETTINGS    extends AvFeatureSettings>
                        extends AntivirusModelImpl<TASKFACTORY, SETTINGS>
                        implements BaseScannerModel<SCANRESULTS>,
                                   ScanObserver<SCANRESULTS> {

    private ScanController mScanController;
    private SCANRESULTS    mScanResults;
    private boolean        mIsScanRunning;
    private boolean        mNeedDisplayScanResults;

    public BaseScannerModelImpl(@NonNull ExecutorService          executorService,
                                @NonNull TASKFACTORY              tasksFactory,
                                @NonNull SETTINGS                 settings,
                                @NonNull ThreatInfoCollector      threatInfoCollector,
                                @NonNull ThreatInfoWrapperFactory threatInfoWrapperFactory,
                                @NonNull ThreadManager            threadManager,
                                @NonNull SdkManager               sdkManager,
                                @NonNull ThreatHandler            threatHandler,
                                @NonNull PackageMonitor           packageMonitor,
                                @NonNull UiLauncher               uiLauncher) {
        super(executorService,
              tasksFactory,
              settings,
              threatInfoCollector,
              threatInfoWrapperFactory,
              threadManager,
              sdkManager,
              threatHandler,
              packageMonitor,
              uiLauncher);

    }

    @SuppressWarnings("unused")
    final protected ScanController getScanController() {
        return mScanController;
    }

    @Override
    @Nullable
    final public SCANRESULTS getScanResults() {
        return mScanResults;
    }

    @Override
    final public void consumeScanResults() {
        mScanResults = null;
    }

    @Override
    @CallSuper
    public boolean startScan() {
        mIsScanRunning = true;
        return true;
    }

    @Override
    @CallSuper
    public void stopScan() {
        mIsScanRunning = false;
        if (mScanController != null) {
            mScanController.stopScan();
        }
    }

    @Override
    @CallSuper
    public void pauseScan() {
        if (mScanController != null) {
            mScanController.pauseScan();
        }
    }

    @Override
    @CallSuper
    public void resumeScan() {
        if (mScanController != null) {
            mScanController.resumeScan();
        }
    }

    @Override
    @CallSuper
    public boolean isScanRunning() {
        return mIsScanRunning;
    }

    @Override
    @CallSuper
    public boolean isScanPaused() {
        if (mScanController == null) {
            return false;
        } else {
            return mScanController.isPaused();
        }
    }

    @Override
    @CallSuper
    public void onScanStarted(@NonNull ScanController controller) {
        mScanController = controller;
        mScanResults    = null;
    }

    @Override
    public void onScanPaused() {
        notifyObservers(BaseScannerModelObserver::onScanPaused, BaseScannerModelObserver.class);
    }

    @Override
    public void onScanResumed() {
        notifyObservers(BaseScannerModelObserver::onScanResumed, BaseScannerModelObserver.class);
    }

    @Override
    public void onBasesUnavailable() {
        mIsScanRunning          = false;
        mScanController         = null;
        notifyObservers(BaseScannerModelObserver::onBasesUnavailable, BaseScannerModelObserver.class);
    }

    @Override
    public void onError(@NonNull ScanErrorType error) {
        mIsScanRunning          = false;
        mScanController         = null;
        notifyObservers(observer -> observer.onError(error), BaseScannerModelObserver.class);
    }

    @Override
    @CallSuper
    public void onScanFinished(@NonNull SCANRESULTS scanResults) {
        mScanResults            = scanResults;
        mScanController         = null;
        mIsScanRunning          = false;
        mNeedDisplayScanResults = true;
        //noinspection unchecked
        notifyObservers(observer -> observer.onScanFinished(scanResults), BaseScannerModelObserver.class);
    }

    @Override
    public void switchToScanView() {
        notifyObservers(
                observer -> observer.onChangeViewRequest(ViewSwitchModelObserver.ViewType.ScanView, null),
                ViewSwitchModelObserver.class
        );
    }

    @Override
    public void switchToScanResultsView() {
        notifyObservers(
                observer -> observer.onChangeViewRequest(ViewSwitchModelObserver.ViewType.ScanResultsView, null),
                ViewSwitchModelObserver.class
        );
    }

    @Override
    public void switchToThreatsInfoView(@NonNull ThreatType threatType) {
        notifyObservers(
                observer -> observer.onChangeViewRequest(ViewSwitchModelObserver.ViewType.ThreatsInfoView, threatType),
                ViewSwitchModelObserver.class
        );
    }

    @Override
    public void switchToOdsApplicationThreats() {
        notifyObservers(
                observer -> observer.onChangeViewRequest(ViewSwitchModelObserver.ViewType.Applications, true),
                ViewSwitchModelObserver.class
        );
    }

    @Override
    public boolean needDisplayScanResults() {
        return mNeedDisplayScanResults && mScanResults != null;
    }

    @Override
    public void resetDisplayScanResults() {
        mNeedDisplayScanResults = false;
    }
}
