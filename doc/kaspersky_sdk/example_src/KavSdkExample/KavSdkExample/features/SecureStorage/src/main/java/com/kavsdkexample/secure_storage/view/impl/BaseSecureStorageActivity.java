/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.view.impl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.EditText;
import android.widget.Toast;

import com.kavsdkexample.core.app.presenter.BasePresenter;
import com.kavsdkexample.secure_storage.view.SecureStorageBaseView;
import com.kavsdkexample.core.ui.BaseActivity;
import com.kavsdkexample.secure_storage.R;
import com.kavsdkexample.secure_storage.R2;
import com.kavsdkexample.secure_storage.view.SecureStorageBaseViewState;

import butterknife.BindView;
import butterknife.ButterKnife;

import dagger.android.HasAndroidInjector;

abstract class BaseSecureStorageActivity<VIEW      extends SecureStorageBaseView,
                                         VIEWSTATE extends SecureStorageBaseViewState,
                                         PRESENTER extends BasePresenter<VIEW, VIEWSTATE>>
                                         extends    BaseActivity<VIEW,VIEWSTATE, PRESENTER>
                                         implements SecureStorageBaseView,
                                                    HasAndroidInjector {

    @BindView(R2.id.editTextFileName)            EditText  mEditTextFileName;
    @BindView(R2.id.editTextPassword)            EditText  mEditTextPassword;

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        ButterKnife.bind(this);
    }

    @Override
    public void setPath(@NonNull String path) {
        mEditTextFileName.setText(path);
    }

    @Override
    @NonNull
    public String getPath() {
        return mEditTextFileName.getText().toString();
    }

    @Override
    @NonNull
    public String getPassword() {
        return mEditTextPassword.getText().toString();
    }


    @Override
    public void setPassword(@NonNull String password) {
        mEditTextPassword.setText(password);
    }


    @Override
    public void showError(@NonNull String error) {
        String message = getString(R.string.str_secure_storage_test_error_occured) + error;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showError(@NonNull ErrorType errorType) {
        String message;
        switch (errorType) {
            case EmptyData:
                message = getString(R.string.fingerprint_error_empty_data);
                break;
            case EmptyPath:
                message = getString(R.string.fingerprint_error_empty_path);
                break;
            case EmptyPassword:
                message = getString(R.string.str_secure_storage_test_error_empty_passwords);
                break;
            case DbAlreadyOpened:
                message = getString(R.string.str_secure_storage_test_database_error_already_opened);
                break;
            case DbNotOpened:
                message = getString(R.string.str_secure_storage_test_database_error_not_opened);
                break;
            default:
                throw new IllegalStateException("Unknown error type");
        }
        showError(message);
    }
}
