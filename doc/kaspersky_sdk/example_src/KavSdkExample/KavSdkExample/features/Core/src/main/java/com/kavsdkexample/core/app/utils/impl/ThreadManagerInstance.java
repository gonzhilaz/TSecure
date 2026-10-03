/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.utils.impl;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.utils.ThreadManager;

public final class ThreadManagerInstance {
    private static final ThreadManager INSTANCE = new ThreadManagerImpl();

    private ThreadManagerInstance() {
    }

    @NonNull
    public static ThreadManager getInstance() {
        return INSTANCE;
    }
}
