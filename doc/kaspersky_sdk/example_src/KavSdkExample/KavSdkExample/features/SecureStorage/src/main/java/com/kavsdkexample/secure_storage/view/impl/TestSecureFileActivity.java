/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.view.impl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kavsdkexample.core.app.ExtendableInjector;
import com.kavsdkexample.secure_storage.R;
import com.kavsdkexample.secure_storage.R2;
import com.kavsdkexample.secure_storage.presenter.file.FilePresenter;
import com.kavsdkexample.secure_storage.view.file.FileView;
import com.kavsdkexample.secure_storage.view.file.FileViewState;
import com.kavsdkexample.secure_storage.view.file.impl.FileViewHelper;
import com.kavsdkexample.secure_storage.view.file.impl.FileViewStateImpl;

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
 * Demonstration of using of Secure File SDK component.
 * This activity allows the user to write to and read from a secure file.
 *
 */
public class TestSecureFileActivity extends    BaseSecureStorageActivity<FileView, FileViewState, FilePresenter>
                                    implements FileView,
                                               View.OnClickListener,
                                               TextWatcher {

    @Inject ExtendableInjector<Object> mExtendableInjector;
    @Inject FilePresenter              mPresenter;

    @BindView(R2.id.buttonPreviousFilePage)        Button       mButtonPreviousFilePage;
    @BindView(R2.id.buttonNextFilePage)            Button       mButtonNextFilePage;
    @BindView(R2.id.buttonWriteFile)               Button       mButtonWriteFile;
    @BindView(R2.id.buttonAppendFile)              Button       mButtonAppendFile;
    @BindView(R2.id.editTextFileName)              EditText     mEditTextFileName;
    @BindView(R2.id.editTextPassword)              EditText     mEditTextFilePassword;
    @BindView(R2.id.editTextUserText)              EditText     mEditTextUserText;
    @BindView(R2.id.large_file_warning_layout)     LinearLayout mLargeFileWarningLayout;

    @BindViews({ R2.id.buttonReadFile,
                 R2.id.buttonWriteFile,
                 R2.id.buttonAppendFile
    })       List<Button> mOperationsButtons;

    private FileViewState mViewState;
    private int           mCurrentPageNum;
    private int           mCountOfPages;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_secure_file_activity);
        AndroidInjection.inject(this);
        ButterKnife.bind(this);
        mViewState = (savedInstanceState == null) ? null : FileViewHelper.fromBundle(savedInstanceState);
    }

    @Override
    public void setOperationsButtonsEnabled(boolean enabled) {
        ViewCollections.run(mOperationsButtons, (Action<View>) (view, index) -> view.setEnabled(enabled));
    }

    @Override
    protected FilePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected FileViewState getViewState() {
        return mViewState;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEditTextFileName.addTextChangedListener(this);
    }

    @Override
    @OnClick({ R2.id.buttonReadFile,
               R2.id.buttonWriteFile,
               R2.id.buttonAppendFile,
               R2.id.buttonPreviousFilePage,
               R2.id.buttonNextFilePage })
    public void onClick(View v) {
        final int id            = v.getId();

        // Can't use switch because R.id variables are not final for library project
        if (id == R.id.buttonReadFile) {
            mPresenter.readFile(0);
        } else if (id == R.id.buttonAppendFile) {
            mPresenter.writeFile(true);
        } else if (id == R.id.buttonWriteFile) {
            mPresenter.writeFile(false);
        } else if (id == R.id.buttonNextFilePage) {
            mPresenter.readFile(mCurrentPageNum + 1);
        } else if (id == R.id.buttonPreviousFilePage) {
            mPresenter.readFile(mCurrentPageNum - 1);
        } else {
            throw new IllegalStateException("Unsupported view id: " + id);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        setDisplayLargeFileWarningLayout(false);
    }


    private void setPreviousNextButtonsStates(int currentPageNum, int pageCount) {
        mButtonPreviousFilePage.setEnabled(currentPageNum != 0);
        mButtonNextFilePage.setEnabled(currentPageNum != pageCount - 1);
    }

    private void setDisplayLargeFileWarningLayout(boolean state) {
        if (state) {
            mLargeFileWarningLayout.setVisibility(View.VISIBLE);
            mButtonWriteFile.setEnabled(false);
            mButtonAppendFile.setEnabled(false);
            mEditTextUserText.setCursorVisible(false);
        } else {
            mLargeFileWarningLayout.setVisibility(View.GONE);
            mButtonWriteFile.setEnabled(true);
            mButtonAppendFile.setEnabled(true);
            mEditTextUserText.setCursorVisible(true);
        }
    }

    @Override
    public void showFileWritten() {
        Toast.makeText(this, "Operation successfully completed", Toast.LENGTH_LONG).show();
        setOperationsButtonsEnabled(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        FileViewHelper.toBundle(outState,
                new FileViewStateImpl(
                        getPath(),
                        getPassword(),
                        getFileContent(),
                        mCurrentPageNum,
                        mCountOfPages));
    }

    @Override
    public void showOverwriteDialog(boolean append) {
        OverwriteDialogFragment.show(getSupportFragmentManager(), append);
    }

    @Override
    @NonNull
    public String getFileContent() {
        return mEditTextUserText.getText().toString();
    }

    @Override
    public void setFileContent(@NonNull String result, int pageNum, int pageCount) {
        mCurrentPageNum = pageNum;
        mCountOfPages   = pageCount;
        mEditTextUserText.setText(result);
        if (pageCount > 1) {
            String pageOfPages = getString(R.string.str_secure_storage_large_file_warning, pageNum + 1, pageCount);
            TextView largeFileWarningText = mLargeFileWarningLayout.findViewById(R.id.large_file_warning_text);
            largeFileWarningText.setText(pageOfPages);

            setDisplayLargeFileWarningLayout(true);
            setPreviousNextButtonsStates(pageNum, pageCount);

        } else {
            setDisplayLargeFileWarningLayout(false);
        }
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return mExtendableInjector;
    }
}