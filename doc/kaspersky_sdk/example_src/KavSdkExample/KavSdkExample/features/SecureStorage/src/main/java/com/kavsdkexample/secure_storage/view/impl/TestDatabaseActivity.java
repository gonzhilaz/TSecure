/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.view.impl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kavsdkexample.core.app.ExtendableInjector;
import com.kavsdkexample.secure_storage.R;
import com.kavsdkexample.secure_storage.R2;
import com.kavsdkexample.secure_storage.presenter.database.DbPresenter;
import com.kavsdkexample.secure_storage.view.database.DbView;
import com.kavsdkexample.secure_storage.view.database.DbViewState;
import com.kavsdkexample.secure_storage.view.database.impl.DbViewHelper;
import com.kavsdkexample.secure_storage.view.database.impl.DbViewStateImpl;

import java.util.List;

import javax.inject.Inject;

import butterknife.Action;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.ViewCollections;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;

/**
 * Demonstration of using Secure Database SDK component.
 * This sample activity allows to work with a secure database,
 * view its tables and execute SQL queries
 */
@UiThread
public class TestDatabaseActivity extends     BaseSecureStorageActivity<DbView, DbViewState, DbPresenter>
                                  implements  DbView,
                                              View.OnClickListener {

    @Inject ExtendableInjector<Object> mExtendableInjector;
    @Inject DbPresenter                mPresenter;

    @BindView(R2.id.editTextSql)                 EditText     mEditTextSql;
    @BindView(R2.id.textViewDatabaseStatus)      TextView     mDatabaseStatusTextView;
    @BindView(R2.id.sql_result)                  TextView     mSqlRequestResultTextView;

    @BindViews({ R2.id.buttonOpenDatabase,
                 R2.id.buttonShowTables,
                 R2.id.buttonCloseDatabase,
                 R2.id.buttonSql,
                 R2.id.buttonTestData,
                 R2.id.buttonDeleteDatabase
    })      List<Button> mOperationsButtons;

    private DbViewState mViewState;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_database_activity);
        AndroidInjection.inject(this);
        ButterKnife.bind(this);
        mViewState = (savedInstanceState == null) ? null : DbViewHelper.fromBundle(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        DbViewHelper.toBundle(savedInstanceState,
            new DbViewStateImpl(mEditTextFileName.getText().toString(),
                                mEditTextPassword.getText().toString(),
                                mEditTextSql.getText().toString(),
                                mSqlRequestResultTextView.getText().toString()));
    }


    @Override
    protected DbPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected DbViewState getViewState() {
        return mViewState;
    }


    @OnClick({ R2.id.buttonOpenDatabase,
               R2.id.buttonCloseDatabase,
               R2.id.buttonDeleteDatabase,
               R2.id.buttonShowTables,
               R2.id.buttonSql,
               R2.id.buttonTestData })
    @Override
    public void onClick(View v) {
        final int id            = v.getId();
        final String path       = mEditTextFileName.getText().toString();
        final String password   = mEditTextPassword.getText().toString();
        // Can't use switch because R.id variables are not final for library project
        if (id == R.id.buttonOpenDatabase) {
            mPresenter.openDatabase(path, password);
        } else if (id == R.id.buttonCloseDatabase) {
            mPresenter.closeDatabase(false);
        } else if (id == R.id.buttonDeleteDatabase) {
            mPresenter.closeDatabase(true);
        } else if (id == R.id.buttonShowTables) {
            mPresenter.executeDatabaseRequest("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name");
        } else if (id == R.id.buttonTestData) {
            mPresenter.createTestTable();
        } else if (id == R.id.buttonSql) {
            mPresenter.executeDatabaseRequest(mEditTextSql.getText().toString());
        }
    }

    @Override
    public void setSqlResult(@NonNull String result) {
        mSqlRequestResultTextView.setText(result);
    }

    @Override
    public void setDatabaseOpened(boolean opened) {
        String statusStr = opened ?
                               getString(R.string.str_secure_storage_test_database_status_opened) :
                               getString(R.string.str_secure_storage_test_database_status_closed);

        mDatabaseStatusTextView.setText(getString(R.string.str_secure_storage_test_database_status, statusStr));
    }

    @Override
    public void setSqlRequest(@NonNull String request) {
        mSqlRequestResultTextView.setText(request);
    }

    @Override
    public void setOperationsButtonsEnabled(boolean enabled) {
        ViewCollections.run(mOperationsButtons, (Action<View>) (view, index) -> view.setEnabled(enabled));
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return mExtendableInjector;
    }
}
