/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.eula.view.impl;

import com.kavsdkexample.eula.view.EulaViewState;

public final class EulaViewStateImpl implements EulaViewState {
    private final boolean mEulaShown;

    public EulaViewStateImpl(boolean eulaShown) {
        mEulaShown = eulaShown;
    }

    @Override
    public boolean isEulaShown() {
        return mEulaShown;
    }
}
