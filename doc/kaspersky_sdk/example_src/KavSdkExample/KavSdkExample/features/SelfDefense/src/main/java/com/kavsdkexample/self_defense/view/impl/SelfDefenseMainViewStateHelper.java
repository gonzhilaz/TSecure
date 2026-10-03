/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.self_defense.view.impl;

import android.os.Bundle;
import androidx.annotation.NonNull;

import com.kavsdkexample.self_defense.view.SelfDefenseMainViewState;

final class SelfDefenseMainViewStateHelper {
    private static final String DISPLAYED_CHILD_POS      = "child_pos";

    private SelfDefenseMainViewStateHelper() {
    }

    @NonNull
    static SelfDefenseMainViewState fromBundle(@NonNull Bundle bundle) {
        return new SelfDefenseMainViewStateImpl(bundle.getInt(DISPLAYED_CHILD_POS,  0));
    }

    static void toBundle(@NonNull Bundle bundle, @NonNull SelfDefenseMainViewState state) {
        bundle.putInt(DISPLAYED_CHILD_POS, state.getDisplayedChildIndex());
    }
}
