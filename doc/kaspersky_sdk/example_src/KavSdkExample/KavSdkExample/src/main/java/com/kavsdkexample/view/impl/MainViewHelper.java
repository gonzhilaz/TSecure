/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.view.impl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.view.MainViewState;

final class MainViewHelper {
    private MainViewHelper() {
    }

    @NonNull
    static MainViewState fromBundle(@NonNull Bundle bundle) {
        return new MainViewStateImpl();
    }

    static void toBundle(@NonNull Bundle bundle, @Nullable MainViewState state) {
    }
}
