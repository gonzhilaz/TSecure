/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.appcontrol.di;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.appcontrol.model.AppControlModel;
import com.kavsdkexample.appcontrol.model.AppControlModelFactory;
import com.kavsdkexample.appcontrol.model.AppControlObserver;
import com.kavsdkexample.appcontrol.model.Settings;
import com.kavsdkexample.appcontrol.model.impl.SettingsImpl;
import com.kavsdkexample.appcontrol.presenter.impl.AppControlObserverImpl;
import com.kavsdkexample.appcontrol.sdk.impl.AppControlModelFactoryImpl;
import com.kavsdkexample.appcontrol.view.WindowManagerBlockView;
import com.kavsdkexample.appcontrol.view.impl.AppBlockActivity;
import com.kavsdkexample.appcontrol.view.impl.WindowManagerBlockViewImpl;
import com.kavsdkexample.core.app.features.TabInfoFactoryAndroid;
import com.kavsdkexample.core.app.features.impl.TabInfoFactoryImpl;
import com.kavsdkexample.core.app.utils.AppThreadPool;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.core.app.utils.impl.ThreadManagerInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
abstract class FeatureModule {
    @Provides
    @NonNull
    static Context provideContext(@NonNull Application application) {
        return application.getApplicationContext();
    }

    @Provides
    @NonNull
    static ExecutorService provideExecutorService() {
        return AppThreadPool.getExecutorService();
    }

    @Provides
    @Singleton
    @NonNull
    static TabInfoFactoryAndroid provideTabInfoFactory() {
        return new TabInfoFactoryImpl();
    }

    @Provides
    @NonNull
    static ThreadManager provideThreadManager() {
        return ThreadManagerInstance.getInstance();
    }

    @Provides
    @Singleton
    @NonNull
    static AppControlModel provideAppControlModel(@NonNull ThreadManager threadManager,
                                                  @NonNull AppControlModelFactory appControlModelFactory,
                                                  @NonNull Context context,
                                                  @NonNull Settings settings) {
        Intent blockingIntent = new Intent(context, AppBlockActivity.class);
        blockingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        blockingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        blockingIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        blockingIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        final ArrayList<String> defaultWhiteList = new ArrayList<>();
        defaultWhiteList.addAll(Arrays.asList(
                context.getPackageName(),
                getCurrentLauncherPackageName(context),
                "com.android.inputmethod.latin",
                "com.android.systemui"
        ));
        defaultWhiteList.addAll(getInputMethods(context));
        return appControlModelFactory.createAppControlModel(
                context,
                threadManager,
                blockingIntent,
                settings,
                defaultWhiteList
        );
    }

    @Provides
    @NonNull
    static Settings provideSettings(@NonNull Context context) {
        return new SettingsImpl(context);
    }

    @Provides
    @NonNull
    static AppControlModelFactory provideAppControlManagerFactory() {
        return new AppControlModelFactoryImpl();
    }

    @Provides
    @Singleton
    @NonNull
    static WindowManagerBlockView provideWindowManagerBlockView(final Context context) {
        return new WindowManagerBlockViewImpl(context);
    }

    @Provides
    @Singleton
    @NonNull
    static AppControlObserver provideAppControlObserver(final WindowManagerBlockView view) {
        return new AppControlObserverImpl(view);
    }

    @Nullable
    private static String getCurrentLauncherPackageName(final Context context) {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo resolveInfo = context
                .getPackageManager()
                .resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return (resolveInfo != null) ? resolveInfo.activityInfo.packageName : null;
    }

    private static List<String> getInputMethods(final Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return new ArrayList<>();
        }
        List<InputMethodInfo> enabledInputMethodList = imm.getEnabledInputMethodList();
        List<String> result = new ArrayList<>(enabledInputMethodList.size());
        for (InputMethodInfo inputMethodInfo : enabledInputMethodList) {
            result.add(inputMethodInfo.getPackageName());
        }
        return result;
    }
}
