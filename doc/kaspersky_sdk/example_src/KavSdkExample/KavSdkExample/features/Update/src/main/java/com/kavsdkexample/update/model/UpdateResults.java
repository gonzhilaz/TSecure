/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.update.model;

public enum UpdateResults {
    TaskStarted,
    ServerChanged,
    ServerSelected,
    BasesDownloaded,
    BasesApplied,
    TaskFinished,
    WaitingForAnotherUpdateFinished,
    ErrorLicenseExpired,
    WrongComponent,
    UpdateSuccess,
    UpdateNoNewBases,
    UpdateFailedNoConnection,
    UpdateFailedNoDiskSpace,
    UpdateFailed,
    UpdateCanceled,
    UpdateCanceledDateIncorrect,
    UpdateBasesCorrupted,
    UpdateFinishedWithResultCode,
    FailedToCreateUpdater,
    MalformedURLException
}
