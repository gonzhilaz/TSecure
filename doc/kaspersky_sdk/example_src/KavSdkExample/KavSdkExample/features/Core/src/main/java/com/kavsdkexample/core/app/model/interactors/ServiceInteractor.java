/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.model.interactors;

import androidx.annotation.NonNull;

public interface ServiceInteractor {
    /**
     * Starts foreground service if it is enabled
     * @param caller Caller type that requests to start service
     * @return true if service was started as foreground or false otherwise
     */
    boolean startService(@NonNull ForegroundCaller caller);
    /**
     * Stops foreground service for this caller. Foreground service stops when it has no
     * active callers (all callers that previously call start than call stop)
     * @param caller Caller type that requests to start service
     */
    void stopService(@NonNull ForegroundCaller caller);
}
