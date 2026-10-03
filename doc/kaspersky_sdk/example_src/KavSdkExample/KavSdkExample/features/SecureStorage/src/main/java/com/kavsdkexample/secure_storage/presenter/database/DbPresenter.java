/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.presenter.database;

import com.kavsdkexample.secure_storage.model.database.DatabaseOperations;
import com.kavsdkexample.secure_storage.presenter.base.SecureStoragePresenter;
import com.kavsdkexample.secure_storage.view.database.DbView;
import com.kavsdkexample.secure_storage.view.database.DbViewState;

public interface DbPresenter extends SecureStoragePresenter<DbView, DbViewState>,
        DatabaseOperations {
}
