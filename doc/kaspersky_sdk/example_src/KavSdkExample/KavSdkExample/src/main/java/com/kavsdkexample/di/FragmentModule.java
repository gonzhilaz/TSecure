/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.di;

import com.kavsdkexample.core.app.di.PerFragment;
import com.kavsdkexample.presenter.MainTabFragmentPresenter;
import com.kavsdkexample.presenter.impl.MainTabFragmentPresenterImpl;
import com.kavsdkexample.presenter.license.LicenseInfoPresenter;
import com.kavsdkexample.presenter.license.impl.LicenseInfoPresenterImpl;
import com.kavsdkexample.presenter.permissions.InsufficientPermissionsPresenter;
import com.kavsdkexample.presenter.permissions.impl.InsufficientPermissionsPresenterImpl;

import dagger.Binds;
import dagger.Module;

@Module
abstract class FragmentModule {
    @PerFragment
    @Binds abstract InsufficientPermissionsPresenter permissionsPresenter(InsufficientPermissionsPresenterImpl presenter);
    @PerFragment
    @Binds abstract LicenseInfoPresenter licensePresenter(LicenseInfoPresenterImpl presenter);
    @PerFragment
    @Binds abstract MainTabFragmentPresenter mainTabFragmentPresenter(MainTabFragmentPresenterImpl presenter);
}
