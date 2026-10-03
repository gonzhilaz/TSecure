/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.update.sdk.impl;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import java.net.URL;
import java.net.MalformedURLException;

import com.kavsdk.license.SdkLicenseViolationException;
import com.kavsdk.updater.UpdateEventListener;
import com.kavsdk.updater.Updater;
import com.kavsdk.updater.UpdaterConstants;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.update.model.Settings;
import com.kavsdkexample.update.model.UpdateController;
import com.kavsdkexample.update.model.UpdateModelComponentMode;
import com.kavsdkexample.update.model.UpdateModelObserver;
import com.kavsdkexample.update.model.UpdateModelUpdateServerMode;
import com.kavsdkexample.update.model.UpdateResults;

public final class UpdateTask implements Runnable, UpdateEventListener, UpdateController {

    private static final boolean DEBUG = Boolean.parseBoolean("true");
    private final ThreadManager mThreadManager;
    private final UpdateModelObserver mUpdateObserver;
    private final Settings mSettings;
    private final Object mLock;
    private Updater mUpdater;
    private boolean mNeedCancel;

    UpdateTask(@NonNull ThreadManager threadManager,
               @NonNull UpdateModelObserver updateObserver,
               @NonNull Settings settings) {
        mThreadManager = threadManager;
        mUpdateObserver = updateObserver;
        mSettings = settings;
        mLock = new Object();
        mNeedCancel = false;
        mUpdater = null;
    }

    @Override
    @WorkerThread
    public void run() {
        if (DEBUG) {
            mThreadManager.checkWorkerThread();
        }

        mThreadManager.runOnUiThread(() -> mUpdateObserver.setController(this));
        mThreadManager.runOnUiThread(mUpdateObserver::updateIsRunning);

        try {
            mUpdater = Updater.getInstance();
            doRun(mUpdater);
        } catch (SdkLicenseViolationException e) {
            mThreadManager.runOnUiThread(() -> mUpdateObserver.executionDetails(UpdateResults.ErrorLicenseExpired));
        } catch (MalformedURLException e) {
            mThreadManager.runOnUiThread(() -> mUpdateObserver.executionDetails(UpdateResults.MalformedURLException, e.getMessage()));
        } finally {
            mThreadManager.runOnUiThread(mUpdateObserver::updateIsCompleted);
            mUpdater = null;
        }
    }

    @Override
    public void cancel() {
        synchronized (mLock) {
            mNeedCancel = true;
        }
    }

    @Override
    public boolean onUpdateEvent(int eventType, int result) {
        Updater updater = mUpdater;
        final String updateUrlString;
        if (updater != null) {
            final URL updateUrl = updater.getUpdateServer();
            updateUrlString = (updateUrl == null) ? "" : updateUrl.toString();
        }
        else {
            updateUrlString = "";
        }
        switch (eventType) {
            case UpdaterConstants.UPDATE_EVENT_TASK_STARTED:
                mThreadManager.runOnUiThread(() -> mUpdateObserver.executionDetails(UpdateResults.TaskStarted));
                break;
            case UpdaterConstants.UPDATE_EVENT_SERVER_CHANGED:
                mThreadManager.runOnUiThread(() -> mUpdateObserver.executionDetails(UpdateResults.ServerChanged, updateUrlString));
                break;
            case UpdaterConstants.UPDATE_EVENT_SERVER_SELECTED:
                mThreadManager.runOnUiThread(() -> mUpdateObserver.executionDetails(UpdateResults.ServerSelected, updateUrlString));
                break;
            case UpdaterConstants.UPDATE_EVENT_BASES_DOWNLOADED:
                mThreadManager.runOnUiThread(() -> mUpdateObserver.executionDetails(UpdateResults.BasesDownloaded));
                break;
            case UpdaterConstants.UPDATE_EVENT_BASES_APPLIED:
                mThreadManager.runOnUiThread(() -> mUpdateObserver.executionDetails(UpdateResults.BasesApplied));
                break;
            case UpdaterConstants.UPDATE_EVENT_TASK_FINISHED:
                switch (result) {
                    case UpdaterConstants.UPDATE_RESULT_DBUPDATE_SUCCESS:
                        mThreadManager.runOnUiThread(() -> mUpdateObserver.executionDetails(UpdateResults.UpdateSuccess));
                        break;
                    case UpdaterConstants.UPDATE_RESULT_DBUPDATE_NO_NEW_BASES:
                        mThreadManager.runOnUiThread(() -> mUpdateObserver.executionDetails(UpdateResults.UpdateNoNewBases));
                        break;
                    case UpdaterConstants.UPDATE_RESULT_DBUPDATE_FAILED_NO_CONNECTION:
                        mThreadManager.runOnUiThread(() -> mUpdateObserver.executionDetails(UpdateResults.UpdateFailedNoConnection));
                        break;
                    case UpdaterConstants.UPDATE_RESULT_DBUPDATE_FAILED_NO_DISK_SPACE:
                        mThreadManager.runOnUiThread(() -> mUpdateObserver.executionDetails(UpdateResults.UpdateFailedNoDiskSpace));
                        break;
                    case UpdaterConstants.UPDATE_RESULT_DBUPDATE_FAILED:
                        mThreadManager.runOnUiThread(() -> mUpdateObserver.executionDetails(UpdateResults.UpdateFailed));
                        break;
                    case UpdaterConstants.UPDATE_RESULT_DBUPDATE_CANCELED:
                        mThreadManager.runOnUiThread(() -> mUpdateObserver.executionDetails(UpdateResults.UpdateCanceled));
                        break;
                    case UpdaterConstants.UPDATE_RESULT_DBUPDATE_CANCELED_DATE_INCORRECT:
                        mThreadManager.runOnUiThread(() -> mUpdateObserver.executionDetails(UpdateResults.UpdateCanceledDateIncorrect));
                        break;
                    case UpdaterConstants.UPDATE_RESULT_DBUPDATE_BASES_CORRUPTED:
                        mThreadManager.runOnUiThread(() -> mUpdateObserver.executionDetails(UpdateResults.UpdateBasesCorrupted));
                        break;
                    default:
                        mThreadManager.runOnUiThread(() -> mUpdateObserver.executionDetails(UpdateResults.UpdateFinishedWithResultCode, result));
                        break;
                }
                break;
            case UpdaterConstants.UPDATE_EVENT_WAITING_FOR_ANOTHER_UPDATE_FINISHED:
                mThreadManager.runOnUiThread(() -> mUpdateObserver.executionDetails(UpdateResults.WaitingForAnotherUpdateFinished, updateUrlString));
                break;
            default:
                break;
        }

        synchronized (mLock) {
            return mNeedCancel;
        }
    }

    private void doRun(Updater updater) throws SdkLicenseViolationException, MalformedURLException {
        UpdateModelUpdateServerMode serverMode = mSettings.getUpdateModelUpdateServer();
        UpdateModelComponentMode componentMode = mSettings.getUpdateModelComponent();
        String url = mSettings.getUpdateServer();
        if (serverMode != UpdateModelUpdateServerMode.Random) {
            new URL(url); // check url
        }

        if (!UpdateTaskHelper.update(updater, componentMode, serverMode, url, this)) {
            mThreadManager.runOnUiThread(() -> mUpdateObserver.executionDetails(UpdateResults.WrongComponent));
        }
    }
}
