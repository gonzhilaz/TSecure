/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.folder_monitor.di;

import com.kavsdkexample.antivirus.folder_monitor.view.impl.FolderMonitorFragment;
import com.kavsdkexample.antivirus.folder_monitor.view.impl.FoldersActionDialogFragment;
import com.kavsdkexample.core.app.di.PerFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class FragmentsBuilder {
    @ContributesAndroidInjector(modules = { FragmentsModule.class })
    @PerFragment
    abstract FolderMonitorFragment bindFolderMonitorFragment();

    @ContributesAndroidInjector(modules = { FragmentsModule.class })
    @PerFragment
    abstract FoldersActionDialogFragment bindFoldersActionDialogFragment();
}
