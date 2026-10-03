/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.presenter.base;

import com.kavsdkexample.secure_storage.presenter.NoticeOperations;
import com.kavsdkexample.secure_storage.view.SecureStorageBaseView;
import com.kavsdkexample.secure_storage.view.SecureStorageBaseViewState;
import com.kavsdkexample.core.app.presenter.BasePresenter;

public interface SecureStoragePresenter<VIEW    extends SecureStorageBaseView,
                                      VIEWSTATE extends SecureStorageBaseViewState>  
                               extends BasePresenter<VIEW, VIEWSTATE>, NoticeOperations {
}
