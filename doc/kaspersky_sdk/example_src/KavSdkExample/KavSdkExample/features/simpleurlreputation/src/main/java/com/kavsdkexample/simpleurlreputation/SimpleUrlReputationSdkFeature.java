/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.simpleurlreputation;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.kavsdkexample.core.app.ExtendableInjector;
import com.kavsdkexample.core.app.features.SdkFeature;
import com.kavsdkexample.core.app.features.TabInfoAndroid;
import com.kavsdkexample.core.app.features.TabInfoFactoryAndroid;
import com.kavsdkexample.core.app.features.impl.SdkFeatureAndroidImpl;
import com.kavsdkexample.core.app.model.interactors.NotificationInteractor;
import com.kavsdkexample.core.app.model.interactors.ServiceInteractor;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.simpleurlreputation.di.DaggerComponent;
import com.kavsdkexample.simpleurlreputation.model.SimpleUrlReputationModel;
import com.kavsdkexample.simpleurlreputation.view.impl.SimpleUrlReputationFragment;

import javax.inject.Inject;

import dagger.android.AndroidInjector;

@Keep
public class SimpleUrlReputationSdkFeature extends SdkFeatureAndroidImpl {

    @Inject
    ExtendableInjector<Object> mExtendableInjector;

    @Inject
    TabInfoFactoryAndroid mTabInfoFactory;

    @Inject
    ThreadManager mThreadManager;

    @Inject
    SimpleUrlReputationModel mModel;

    public SimpleUrlReputationSdkFeature(@NonNull Application application,
                                         @NonNull ServiceInteractor serviceInteractor,
                                         @NonNull NotificationInteractor notificationInteractor) {
        super(application, serviceInteractor, notificationInteractor);
        DaggerComponent.builder().application(application).build().inject(this);
        if (BuildConfig.DEBUG) {
            mThreadManager.checkUiThread();
        }
        mModel.setSdkStatusProvider(this);
    }

    @NonNull
    @Override
    public String getName() {
        return SdkFeature.SIMPLE_URL_REPUTATION;
    }

    @Override
    public TabInfoAndroid provideFeatureTab(@Nullable TabCompletionCallback callback) {
        return mTabInfoFactory.createTabInfo(mContext.getString(R.string.str_simple_url_reputation_feature_tab), SimpleUrlReputationFragment.class.getName());
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
