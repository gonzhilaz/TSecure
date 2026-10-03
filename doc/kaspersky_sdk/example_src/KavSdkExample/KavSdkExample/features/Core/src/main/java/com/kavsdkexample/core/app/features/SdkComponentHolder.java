/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.features;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class SdkComponentHolder<T> {
    private final T mComponent;
    private final Exception mInitializationException;

    public SdkComponentHolder(@Nullable T component, @Nullable Exception exception) {
        mComponent               = component;
        mInitializationException = exception;
    }

    @NonNull
    public final T required() {
        if (mComponent == null) {
            if (mInitializationException == null) {
                throw new IllegalStateException("Feature component is not initialized");
            } else {
                throw new IllegalStateException("Feature component initialization failed", mInitializationException);
            }
        }
        return mComponent;
    }

    @Nullable
    public final T get() {
        return mComponent;
    }

    @Nullable
    public final Exception getInitializationException() {
        return mInitializationException;
    }

    public final boolean isInitialized() {
        return mComponent != null;
    }
}
