/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.utils;

import com.kavsdkexample.core.app.utils.impl.ThreadFactoryImpl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class AppThreadPool {
    private static final int  IDLE_THREADS_COUNT = 1;
    private static final int  MAX_THREADS_COUNT  = 4;
    private static final long KEEP_ALIVE_TIME    = 60;
    private static volatile ExecutorService sExecutorService;

    private AppThreadPool() {
    }

    public static ExecutorService getExecutorService() {
        ExecutorService executorService = sExecutorService;
        if (executorService == null) {
            synchronized (AppThreadPool.class) {
                executorService = sExecutorService;

                if (executorService == null) {
                    executorService =
                            new ThreadPoolExecutor(
                                    IDLE_THREADS_COUNT,
                                    MAX_THREADS_COUNT,
                                    KEEP_ALIVE_TIME,
                                    TimeUnit.SECONDS,
                                    new LinkedBlockingQueue<>(),
                                    new ThreadFactoryImpl());
                    sExecutorService = executorService;
                }
            }
        }
        return executorService;
    }
}
