/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.eula.view.impl;

import android.os.Bundle;
import androidx.annotation.NonNull;

import com.kavsdkexample.eula.view.EulaViewState;

final class EulaViewHelper {
    private static final String EULA_SHOWN_KEY      = "eula_shown";

    private EulaViewHelper() {
    }

    @NonNull
    static EulaViewState fromBundle(@NonNull Bundle bundle) {
        return new EulaViewStateImpl(bundle.getBoolean(EULA_SHOWN_KEY,  false));
    }

    static void toBundle(@NonNull Bundle bundle, @NonNull EulaViewState state) {
        bundle.putBoolean(EULA_SHOWN_KEY, state.isEulaShown());
    }
}
