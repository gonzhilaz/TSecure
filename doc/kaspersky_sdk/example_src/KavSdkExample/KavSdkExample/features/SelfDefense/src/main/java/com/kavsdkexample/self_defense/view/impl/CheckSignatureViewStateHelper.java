/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.self_defense.view.impl;

import android.os.Bundle;
import androidx.annotation.NonNull;

import com.kavsdkexample.self_defense.view.CheckSignatureViewState;

final class CheckSignatureViewStateHelper {
    private static final String CHECK_RESULT = "check_result";

    private CheckSignatureViewStateHelper() {
    }

    @NonNull
    static CheckSignatureViewState fromBundle(@NonNull Bundle bundle) {
        return new CheckSignatureViewStateImpl(bundle.getString(CHECK_RESULT, ""));
    }

    static void toBundle(@NonNull Bundle bundle, @NonNull CheckSignatureViewState state) {
        bundle.putString(CHECK_RESULT, state.getCheckResult());
    }
}
