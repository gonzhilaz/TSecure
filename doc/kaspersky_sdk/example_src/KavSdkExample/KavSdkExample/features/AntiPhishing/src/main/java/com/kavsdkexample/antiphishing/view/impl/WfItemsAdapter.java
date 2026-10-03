/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antiphishing.view.impl;

import com.kavsdkexample.antiphishing.R;
import com.kavsdkexample.antiphishing.presenter.WebFilterPresenter;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


/**
 * This is a listView adapter for web filter exclusions
 */
class WfItemsAdapter extends BaseAdapter {
    private final LayoutInflater                  mInflater;
    private final WebFilterPresenter              mPresenter;
    private final int                             mTextColor;


    WfItemsAdapter(@NonNull Context context,
                   @NonNull WebFilterPresenter webFilterPresenter) {
        mInflater  = LayoutInflater.from(context);
        mPresenter = webFilterPresenter;
        mTextColor = context.getResources().getColor(R.color.text_color);
    }

    @Override
    public int getCount() {
        return mPresenter.getExclusionsCount();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(android.R.layout.simple_list_item_1, null);
        }
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setTextColor(mTextColor);

        String text = mPresenter.getExclusionAt(position);
        textView.setText(text == null ? "": text);

        return convertView;
    }
}
