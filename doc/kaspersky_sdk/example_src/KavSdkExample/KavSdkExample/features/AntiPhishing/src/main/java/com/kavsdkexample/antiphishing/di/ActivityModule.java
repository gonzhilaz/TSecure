/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antiphishing.di;

import androidx.annotation.NonNull;

import com.kavsdkexample.antiphishing.presenter.WebFilterPresenter;
import com.kavsdkexample.antiphishing.presenter.impl.WebFilterPresenterImpl;
import com.kavsdkexample.core.app.di.PerActivity;

import dagger.Binds;
import dagger.Module;

@Module
abstract class ActivityModule {
    @PerActivity
    @Binds
    @NonNull
    abstract WebFilterPresenter bindAppCategoryPresenter(@NonNull WebFilterPresenterImpl presenter);

}


