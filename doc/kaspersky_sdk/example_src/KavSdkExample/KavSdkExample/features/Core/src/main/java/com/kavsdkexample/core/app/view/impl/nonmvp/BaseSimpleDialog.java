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
 * This class is a base for all dialogs which require string entering.
 * The dialog contains an edit field for string and buttons for string checking and operation canceling.
 * The result of checking is shown as a toast.
 */
public abstract class BaseSimpleDialog extends BaseAsyncOperationDialog {
    protected TextView mTextView;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(getTitleId());
        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //GUI initialization
        View view = inflater.inflate(R.layout.base_simple_dialog, container, false);

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

        Button checkButton = view.findViewById(R.id.checkButton);
        checkButton.setText(getCheckButtonLabelId());
        checkButton.setOnClickListener(this::checkAndShowResult);

        Button cancelButton = view.findViewById(R.id.cancelButton);
        cancelButton.setText(getCancelButtonLabelId());
        cancelButton.setOnClickListener(v -> dismiss());
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
