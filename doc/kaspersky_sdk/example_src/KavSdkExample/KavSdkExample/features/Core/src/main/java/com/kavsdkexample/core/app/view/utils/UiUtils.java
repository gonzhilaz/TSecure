/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.view.utils;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.kavsdkexample.core.R;

import java.util.List;

public final class UiUtils {
    private UiUtils() {
    }

    public static void injectUi(@NonNull FragmentActivity activity, @NonNull ViewGroup viewGroup, int layoutId) {
        injectUi(activity, viewGroup, layoutId, -1);
    }
    public static void injectUi(@NonNull FragmentActivity activity, @NonNull ViewGroup viewGroup, int layoutId, int position) {
        @SuppressLint("InflateParams")
        final ViewGroup contentViewGroup = (ViewGroup) LayoutInflater.from(activity).inflate(layoutId, null);
        int viewCount = contentViewGroup.getChildCount();

        for (int i = 0; i < viewCount; ++i) {
            View currentView = contentViewGroup.getChildAt(0);
            contentViewGroup.removeView(currentView);
            if (position == -1) {
                viewGroup.addView(currentView);
            } else {
                viewGroup.addView(currentView, position);
            }
        }
    }

    public static void silentRadioCheck(@NonNull RadioGroup radioGroup, int checkId, RadioGroup.OnCheckedChangeListener listener) {
        radioGroup.setOnCheckedChangeListener(null);
        radioGroup.check(checkId);
        radioGroup.setOnCheckedChangeListener(listener);
    }

    public static void setCheckedSilent(@NonNull CompoundButton button, boolean checked, CompoundButton.OnCheckedChangeListener listener) {
        button.setOnCheckedChangeListener(null);
        button.setChecked(checked);
        button.setOnCheckedChangeListener(listener);
    }

    public static void setTextSilent(@NonNull TextView textView, String text, @NonNull List<TextWatcher> listeners) {
        for (TextWatcher listener : listeners) {
            textView.removeTextChangedListener(listener);
        }
        textView.setText(text);
        for (TextWatcher listener : listeners) {
            textView.addTextChangedListener(listener);
        }
    }

    public static void setTextSilent(@NonNull TextView textView, String text, TextWatcher listener) {
        textView.removeTextChangedListener(listener);
        textView.setText(text);
        textView.addTextChangedListener(listener);
    }

    /**
     * The method initializes a {@link RadioButton} control
     *
     * @param view a parent view of the RadioButton
     * @param id an id of the RadioButton control
     * @param listener {@link View.OnClickListener} of the control
     * @return an initialized RadioButton control
     */
    public static RadioButton initRadioButton(@NonNull View view, int id, View.OnClickListener listener) {
        RadioButton radioButton = view.findViewById(id);
        radioButton.setEnabled(true);
        radioButton.setOnClickListener(listener);
        return radioButton;
    }


    public static void initCommandView(@NonNull View view, int id, int stringId, View.OnClickListener listener) {
        View commandView = view.findViewById(id);
        commandView.setOnClickListener(listener);
        TextView text = commandView.findViewById(R.id.commandCaption);
        text.setText(stringId);
    }
}
