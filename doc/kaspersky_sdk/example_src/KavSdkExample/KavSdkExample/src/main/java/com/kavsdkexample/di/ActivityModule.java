/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.di;

import com.kavsdkexample.core.app.di.PerActivity;
import com.kavsdkexample.presenter.MainPresenter;
import com.kavsdkexample.presenter.impl.MainPresenterImpl;
import com.kavsdkexample.presenter.proxy.ProxyAuthPresenter;
import com.kavsdkexample.presenter.proxy.impl.ProxyAuthPresenterImpl;

import dagger.Binds;
import dagger.Module;

@Module
abstract class ActivityModule {
    @PerActivity
    @Binds abstract MainPresenter mainPresenter(MainPresenterImpl presenter);

    @PerActivity
    @Binds abstract ProxyAuthPresenter proxyAuthPresenter(ProxyAuthPresenterImpl presenter);
 }
