/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.view.impl.nonmvp;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.kavsdkexample.core.R;
import com.kavsdkexample.core.ui.controls.AutoCompleteTextViewShowingDropDownAlways;

/**
 * This class is a base for dialogs which require string entering.
 * The dialog contains an edit field for string and buttons for string checking and operation canceling.
 * The result of checking is shown as a string in the bottom of the dialog.
 */
public abstract class BaseTextDialog extends BaseAsyncOperationDialog {
    protected TextView mTextView;
    protected Button mCheckButton;
    protected Button mCancelButton;
    protected TextView mResultView;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(getTitleId());
        return dialog;
    }

    @Override
    protected boolean showToast() {
        return false;
    }

    @Override
    protected void onCheckCompleted(String result) {
        mResultView.setText(result);
        mCheckButton.setEnabled(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.base_text_dialog, container, false);

        ArrayAdapter<?> adapter = getAdapter();

        if (adapter == null) {
            mTextView = view.findViewById(R.id.text);
            view.findViewById(R.id.textWithDropDown).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.text).setVisibility(View.GONE);
            AutoCompleteTextViewShowingDropDownAlways textView = view.findViewById(R.id.textWithDropDown);
            textView.setAdapter(adapter);
            mTextView = textView;
        }

        int hintId = getHintTextId();
        if (hintId != View.NO_ID) {
            ((TextView) view.findViewById(R.id.hintText)).setText(hintId);
        }

        int textId = getTextId();
        if (textId != View.NO_ID) {
            mTextView.setText(textId);
        }

        mCheckButton = view.findViewById(R.id.checkButton);
        mCheckButton.setText(getCheckButtonLabelId());
        mCheckButton.setOnClickListener(this::checkAndShowResult);

        mCancelButton = view.findViewById(R.id.cancelButton);
        mCancelButton.setText(getCancelButtonLabelId());
        mCancelButton.setOnClickListener(v -> dismiss());

        mResultView = view.findViewById(R.id.resultText);

        return view;
    }

    protected abstract int getHintTextId();

    protected abstract int getTitleId();

    protected abstract ArrayAdapter<?> getAdapter();

    protected int getCheckButtonLabelId() {
        return R.string.str_core_check_button;
    }

    protected int getCancelButtonLabelId() {
        return R.string.str_core_cancel_button;
    }

    protected int getTextId() {
        return View.NO_ID;
    }
}
