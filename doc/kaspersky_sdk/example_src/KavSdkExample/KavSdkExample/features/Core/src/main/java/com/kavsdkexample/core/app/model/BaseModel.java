/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.model;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.kavsdkexample.core.app.SdkStatusObserver;
import com.kavsdkexample.core.app.SdkStatusProvider;

@UiThread
public interface BaseModel extends SdkStatusObserver {
    void    setSdkStatusProvider(@NonNull SdkStatusProvider provider);
    boolean isLoading();
    boolean isInitialized();

    /**
     * Adds observer for sdk state changes.
     * WARNING: Do not call this function in observer callbacks
     * @param observer instance that wants to observer model state changes
     */
    void    addObserver(@NonNull SdkStatusObserver observer);

    /**
     * Removes previously added observer.
     * WARNING: Do not call this function in observer callbacks
     * @param observer instance of previously added observer
     */
    void    removeObserver(@NonNull SdkStatusObserver observer);

    /**
     * Adds observer for any custom model events.
     * WARNING: Do not call this function in observer callbacks
     * @param observer instance that wants to observe model state changes
     */
    <X>
    void    addAdditionalObserver(@NonNull X observer);

    /**
     * Adds observer for any custom model events.
     * WARNING: Do not call this function in observer callbacks
     * @param observer instance that wants to observe model state changes
     * @param isStatic should this observer receive events from other models instances
     */
    <X>
    void    addAdditionalObserver(@NonNull X observer, boolean isStatic);

    /**
     * Removes previously added observer.
     * WARNING: Do not call this function in observer callbacks
     * @param observer instance of previously added observer
     */
    <X>
    void    removeAdditionalObserver(@NonNull X observer);

    /**
     * Removes previously added observer.
     * WARNING: Do not call this function in observer callbacks
     * @param observer instance of previously added observer
     * @param isStatic should this observer receive events from other models instances
     */
    <X>
    void    removeAdditionalObserver(@NonNull X observer, boolean isStatic);
}
