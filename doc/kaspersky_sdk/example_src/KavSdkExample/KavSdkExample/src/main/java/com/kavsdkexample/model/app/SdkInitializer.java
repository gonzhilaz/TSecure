/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.app;

import androidx.annotation.Nullable;

import com.kavsdkexample.core.app.SdkStatusObserver;

public interface SdkInitializer {
    /**
     * Initialise SDK if it is required
     * @param observer observer will be notified when initialization will be finished, but only if it was started
     * @param ignoreWizard provide false to ignore wizard (used by kashell)
     * @return true if initialization was started or false otherwise
     */
    boolean maybeInitSdkAndFeatures(@Nullable SdkStatusObserver observer, boolean ignoreWizard);
}
