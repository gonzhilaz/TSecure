/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.eula.di;

import com.kavsdkexample.core.app.di.PerFragment;
import com.kavsdkexample.eula.presenter.EulaPresenter;
import com.kavsdkexample.eula.presenter.impl.EulaPresenterImpl;

import dagger.Binds;
import dagger.Module;

@Module
abstract class FragmentsModule {
    @PerFragment
    @Binds abstract EulaPresenter dbPresenter(EulaPresenterImpl presenter);
 }


