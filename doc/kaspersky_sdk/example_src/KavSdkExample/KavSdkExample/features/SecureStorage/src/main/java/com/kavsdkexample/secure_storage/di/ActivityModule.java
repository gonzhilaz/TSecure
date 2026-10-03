/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.di;

import com.kavsdkexample.secure_storage.presenter.NoticeOperations;
import com.kavsdkexample.secure_storage.presenter.database.DbPresenter;
import com.kavsdkexample.secure_storage.presenter.database.impl.DbPresenterImpl;
import com.kavsdkexample.secure_storage.presenter.file.FilePresenter;
import com.kavsdkexample.secure_storage.presenter.file.OverwriteOperation;
import com.kavsdkexample.secure_storage.presenter.file.impl.FilePresenterImpl;
import com.kavsdkexample.core.app.di.PerActivity;

import javax.inject.Named;

import dagger.Binds;
import dagger.Module;

@Module
abstract class ActivityModule {
    @PerActivity
    @Binds abstract DbPresenter        dbPresenter(DbPresenterImpl presenter);
    @PerActivity
    @Named("database")
    @Binds abstract NoticeOperations   noticeDbPresenter(DbPresenter presenter);
    @PerActivity
    @Binds abstract FilePresenter      filePresenter(FilePresenterImpl presenter);
    @PerActivity
    @Named("file")
    @Binds abstract NoticeOperations   noticeFilePresenter(FilePresenter presenter);
    @PerActivity
    @Binds abstract OverwriteOperation overwritePresenter(FilePresenter presenter);
 }


