/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.self_defense.view.impl;

import com.kavsdkexample.self_defense.view.SelfDefenseMainViewState;

public final class SelfDefenseMainViewStateImpl implements SelfDefenseMainViewState {
    private final int mDisplayedChildPos;

    SelfDefenseMainViewStateImpl(int displayedChildPos) {
        mDisplayedChildPos = displayedChildPos;
    }

    @Override
    public int getDisplayedChildIndex() {
        return mDisplayedChildPos;
    }
}
