/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.ui.controls;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class AutoCompleteTextViewShowingDropDownAlways extends AppCompatAutoCompleteTextView {

    public AutoCompleteTextViewShowingDropDownAlways(Context context) {
        super(context);
    }

    public AutoCompleteTextViewShowingDropDownAlways(Context context, AttributeSet arg1) {
        super(context, arg1);
    }

    public AutoCompleteTextViewShowingDropDownAlways(Context context, AttributeSet arg1, int arg2) {
        super(context, arg1, arg2);
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            performFiltering(getText(), 0);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        performFiltering(getText(), 0);
        return super.onTouchEvent(event);
    }

}