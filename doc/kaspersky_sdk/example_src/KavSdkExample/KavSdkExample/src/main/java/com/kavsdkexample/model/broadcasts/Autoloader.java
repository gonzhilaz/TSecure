/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kavsdkexample.core.app.model.interactors.ForegroundCaller;
import com.kavsdkexample.core.app.model.interactors.ServiceInteractor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class Autoloader extends BroadcastReceiver {

    private static final Set<String> ACTIONS = new HashSet<>(Arrays.asList(
        "android.intent.action.BOOT_COMPLETED",
        "android.intent.action.QUICKBOOT_POWERON",
        "android.intent.action.PACKAGE_REPLACED",
        "android.intent.action.MY_PACKAGE_REPLACED"));

    @Inject
    ServiceInteractor mServiceInteractor;

    @Override
    public void onReceive(Context context, Intent intent) {
        AndroidInjection.inject(this, context);
        if (ACTIONS.contains(intent.getAction())) {
            mServiceInteractor.startService(ForegroundCaller.SelfDefense);
        }
    }
}
