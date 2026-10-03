/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.di;

import com.kavsdkexample.secure_storage.view.impl.OverwriteDialogFragment;
import com.kavsdkexample.core.app.di.PerFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class FragmentsBuilder {
    @ContributesAndroidInjector
    @PerFragment
    abstract OverwriteDialogFragment bindOverwriteDialog();
}
