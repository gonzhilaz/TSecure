/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antiphishing.model.impl;

import androidx.annotation.NonNull;

import com.kavsdkexample.antiphishing.model.WebFilterInitObserver;
import com.kavsdkexample.antiphishing.model.WebFilterModelObserver;
import com.kavsdkexample.antiphishing.model.tasks.TasksFactory;
import com.kavsdkexample.antiphishing.repository.sdk.WebFilterManager;
import com.kavsdkexample.antiphishing.repository.settings.Settings;
import com.kavsdkexample.antiphishing.repository.sdk.SdkLocalStatusObserver;
import com.kavsdkexample.core.app.model.impl.BaseModelImpl;
import com.kavsdkexample.antiphishing.model.WebFilterModel;
import com.kavsdkexample.core.app.model.interactors.ForegroundCaller;
import com.kavsdkexample.core.app.model.interactors.NotificationInteractor;
import com.kavsdkexample.core.app.model.interactors.ServiceInteractor;
import com.kavsdkexample.core.app.utils.ThreadManager;

import java.util.concurrent.ExecutorService;

public final class WebFilterModelImpl extends    BaseModelImpl
                                      implements WebFilterModel,
                                                 WebFilterInitObserver {
    private final ThreadManager    mThreadManager;
    private final ExecutorService  mExecutorService;
    private final TasksFactory     mTasksFactory;
    private final WebFilterManager mWebFilterManager;
    private final Settings         mSettings;
    private       Exception        mWebFilterInitException;
    private       boolean          mWebFilterInited;
    private final SdkLocalStatusObserver mSdkLocalStatusObserver;
    private ServiceInteractor mServiceInteractor;


    public WebFilterModelImpl(@NonNull ThreadManager    threadManager,
                              @NonNull ExecutorService  executorService,
                              @NonNull TasksFactory     tasksFactory,
                              @NonNull WebFilterManager webFilterManager,
                              @NonNull Settings         settings) {
        mThreadManager    = threadManager;
        mExecutorService  = executorService;
        mTasksFactory     = tasksFactory;
        mWebFilterManager = webFilterManager;
        mSettings         = settings;
        mSdkLocalStatusObserver = mTasksFactory.createSdkLocalStatusObserver(this, mThreadManager);
    }

    @Override
    public void onSdkInited() {
        super.onSdkInited();
        initWebFilter();
    }

    @Override
    public void initWebFilter() {
        mExecutorService.execute(mTasksFactory.createWebFilterManagerInitTask(mThreadManager, this));
    }

    @Override
    public <OBSERVER> void addAdditionalObserver(@NonNull OBSERVER observer) {
        if (observer instanceof WebFilterModelObserver) {
            if (mWebFilterInited) {
                ((WebFilterModelObserver) observer).onWebFilterInitSuccess();
            } else if (mWebFilterInitException != null) {
                ((WebFilterModelObserver) observer).onWebFilterInitFailed(mWebFilterInitException);
            }
        }
        super.addAdditionalObserver(observer);
    }

    @Override
    public boolean getSavedWebFilter() {
        return mSettings.getWebFilterState();
    }

    @Override
    public boolean getSavedExtendedCategories() {
        return mSettings.getExtCategoriesState();
    }

    @Override
    public boolean getSavedIgnorePowerSafeMode() {
        return mSettings.getIgnorePowerSaveModeState();
    }

    @Override
    public int getSavedProxyPort() {
        return mSettings.getProxyPort();
    }

    @Override
    public boolean getSavedWifiProxy() {
        return mSettings.getWifiProxyState();
    }

    @Override
    public void enableWebFiltering(boolean isEnabled) {
        mWebFilterManager.enableWebFiltering(isEnabled);
        mSettings.saveWebFilterState(isEnabled);
    }

    @Override
    public void enableExtCategories(boolean isEnabled) {
        mSettings.saveExtCategoriesState(isEnabled);
        mWebFilterManager.enableExtCategories(isEnabled);
    }

    @Override
    public void enableIgnorePowerSaveMode(boolean isEnabled) {
        mSettings.saveIgnorePowerSaveModeState(isEnabled);
        mWebFilterManager.enableIgnorePowerSaveMode(isEnabled);
        if (isEnabled) {
            mServiceInteractor.startService(ForegroundCaller.WebFilter);
        } else {
            mServiceInteractor.stopService(ForegroundCaller.WebFilter);
        }
    }

    @Override
    public void enableWifiProxy(int proxyPort, boolean isEnabled) {
        mSettings.saveProxyPort(proxyPort);
        mSettings.saveWifiProxyState(isEnabled);
        initWebFilter();
    }

    @Override
    public int getExclusionsCount() {
        return mWebFilterManager.getExclusionsCount();
    }

    @Override
    public String getExclusionAt(int index) {
        return mWebFilterManager.getExclusionAt(index);
    }

    @Override
    public void onWebFilterInitSuccess() {
        mWebFilterInited = true;
        notifyObservers(WebFilterModelObserver::onWebFilterInitSuccess, WebFilterModelObserver.class);
    }

    @Override
    public void onWebFilterInitFailed(@NonNull Exception e) {
        mWebFilterInitException = e;
        notifyObservers(observer -> observer.onWebFilterInitFailed(e), WebFilterModelObserver.class);
    }

    @Override
    public boolean isWebFilterInitialised() {
        return mWebFilterManager.isWebFilterInitialised();
    }

    @Override
    public CharSequence[] getCategoryNames() {
        return mWebFilterManager.getCategoryNames();
    }

    @Override
    public boolean[] getCheckedItems() {
        return mWebFilterManager.getCheckedItems();
    }

    @Override
    public void setCategoryEnabled(int which, boolean isChecked) {
        mWebFilterManager.setCategoryEnabled(which, isChecked);
    }

    @Override
    public void saveCategories() {
        mWebFilterManager.saveCategories();
    }

    @Override
    public void openAccessibilitySettings() {
        mWebFilterManager.openAccessibilitySettings();
    }

    @Override
    public void changeProxyPort() {
        mWebFilterManager.restoreWifiProxySettings();
        notifyObservers(WebFilterModelObserver::changeProxyPort, WebFilterModelObserver.class);
    }

    @Override
    public void notificationOfWbFilterNotWorking() {
        notifyObservers(WebFilterModelObserver::notificationOfWbFilterNotWorking, WebFilterModelObserver.class);
    }

    @Override
    public void notificationOfTaskReputationNotWorking() {
        notifyObservers(WebFilterModelObserver::notificationOfTaskReputationNotWorking, WebFilterModelObserver.class);
    }

    @Override
    public void addExclusion(String url) {
        mWebFilterManager.addExclusion(url);
    }

    @Override
    public void saveExclusions() {
        mWebFilterManager.saveExclusions();
    }

    @Override
    public void removeExclusion(int index) {
        mWebFilterManager.removeExclusion(index);
    }

    @Override
    public void setInteractors(@NonNull ServiceInteractor serviceInteractor, @NonNull NotificationInteractor notificationInteractor) {
        mServiceInteractor = serviceInteractor;
    }
}