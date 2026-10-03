/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.fragment.app.FragmentActivity;
import android.view.ViewGroup;

import com.kavsdkexample.core.app.features.impl.SdkFeatureAndroidImpl;
import com.kavsdkexample.core.app.model.interactors.NotificationInteractor;
import com.kavsdkexample.core.app.model.interactors.ServiceInteractor;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.secure_storage.di.DaggerComponent;
import com.kavsdkexample.secure_storage.model.database.DbModel;
import com.kavsdkexample.secure_storage.model.file.FileModel;
import com.kavsdkexample.secure_storage.view.impl.FeatureClickListener;
import com.kavsdkexample.core.app.ExtendableInjector;
import com.kavsdkexample.core.app.features.SdkFeature;
import com.kavsdkexample.core.app.view.utils.UiUtils;

import javax.inject.Inject;

import dagger.android.AndroidInjector;

@Keep
public final class SecureStorageSdkFeature extends SdkFeatureAndroidImpl {

    @Inject ExtendableInjector<Object> mExtendableInjector;
    @Inject DbModel                    mDbModel;
    @Inject FileModel                  mFileModel;
    @Inject ThreadManager              mThreadManager;

    @UiThread
    public SecureStorageSdkFeature(@NonNull Application            application,
                                   @NonNull ServiceInteractor      serviceInteractor,
                                   @NonNull NotificationInteractor notificationInteractor) {
        super(application, serviceInteractor, notificationInteractor);
        DaggerComponent.builder()
            .application(application)
            .build()
            .inject(this);

        if (BuildConfig.DEBUG) {
            mThreadManager.checkUiThread();
        }
        mDbModel.setSdkStatusProvider(this);
        mFileModel.setSdkStatusProvider(this);
    }

    @Override
    @NonNull
    public String getName() {
        return SdkFeature.SECURE_STORAGE;
    }

    @Override
    @UiThread
    public void initUiView(@NonNull FragmentActivity activity, @NonNull ViewGroup viewGroup) {
        if (BuildConfig.DEBUG) {
            mThreadManager.checkUiThread();
        }
        UiUtils.injectUi(activity, viewGroup, R.layout.secure_storage_main);

        FeatureClickListener listener = new FeatureClickListener(activity);
        UiUtils.initCommandView(viewGroup, R.id.test_database_button, R.string.str_secure_storage_test_database_button, listener);
        UiUtils.initCommandView(viewGroup, R.id.test_secure_file_button, R.string.str_secure_storage_test_secure_file_button, listener);
    }

    @Override
    public boolean checkHandleIntent(@Nullable Intent intent, @Nullable FragmentActivity parent) {
        return false;
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return mExtendableInjector;
    }
}
