/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.self_defense.di;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.di.PerFragment;
import com.kavsdkexample.self_defense.model.sdk.SdkIntentProvider;
import com.kavsdkexample.self_defense.model.sdk.impl.SdkManagerImpl;
import com.kavsdkexample.self_defense.presenter.CheckSignaturePresenter;
import com.kavsdkexample.self_defense.presenter.SelfDefenseFeaturePresenter;
import com.kavsdkexample.self_defense.presenter.SelfDefenseMainPresenter;
import com.kavsdkexample.self_defense.presenter.impl.CheckSignaturePresenterImpl;
import com.kavsdkexample.self_defense.presenter.impl.SelfDefenseFeaturePresenterImpl;
import com.kavsdkexample.self_defense.presenter.impl.SelfDefenseMainPresenterImpl;

import dagger.Binds;
import dagger.Module;

@Module
abstract class FragmentsModule {
    @PerFragment
    @Binds
    @NonNull
    abstract SelfDefenseFeaturePresenter selfDefenseFeaturePresenter(SelfDefenseFeaturePresenterImpl presenter);

    @PerFragment
    @Binds
    @NonNull
    abstract SelfDefenseMainPresenter selfDefenseMainPresenter(SelfDefenseMainPresenterImpl presenter);

    @PerFragment
    @Binds
    @NonNull
    abstract CheckSignaturePresenter checkSignaturePresenter(CheckSignaturePresenterImpl presenter);

    @PerFragment
    @Binds
    @NonNull
    abstract SdkIntentProvider provideSdkIntentProvider(@NonNull SdkManagerImpl sdkManager);

 }


