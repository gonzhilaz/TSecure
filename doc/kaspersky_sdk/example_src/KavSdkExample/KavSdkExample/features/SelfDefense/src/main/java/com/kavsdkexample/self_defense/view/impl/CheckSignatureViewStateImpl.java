/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.self_defense.view.impl;

import androidx.annotation.NonNull;

import com.kavsdkexample.self_defense.view.CheckSignatureViewState;

public final class CheckSignatureViewStateImpl implements CheckSignatureViewState {
    @NonNull private final String mCheckResult;

    CheckSignatureViewStateImpl(@NonNull String checkResult) {
        mCheckResult = checkResult;
    }

    @Override
    @NonNull
    public String getCheckResult() {
        return mCheckResult;
    }
}
