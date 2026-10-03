/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.view.impl;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.kavsdkexample.core.app.features.TabInfoAndroid;
import com.kavsdkexample.core.ui.controls.BaseFragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

final class TabsAdapter extends BaseFragmentStatePagerAdapter {
    private static final String TABS_KEY = "tabs_info";
    private final Context mContext;
    private ArrayList<TabInfoAndroid> mTabs;

    TabsAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
        mTabs    = new ArrayList<>();
    }

    private TabInfoAndroid getTab(int position) {
        return mTabs.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        TabInfoAndroid tab = getTab(position);
        return Fragment.instantiate(mContext, tab.getTabClassName(), null);
    }

    @Override
    public int getCount() {
        return mTabs.size();
    }

    // overloading of saveState() does not work correct because ViewPager first call getCount on empty
    // adapter, than restores it states and than crashes because first call to getCount != second call
    // to getCount
    @SuppressWarnings("WeakerAccess")
    public void onSaveState(@NonNull Bundle bundle) {
        int tabsCount = mTabs.size();
        if (tabsCount != 0) {
            bundle.putParcelableArrayList(TABS_KEY, mTabs);
        }
    }


    @SuppressWarnings("WeakerAccess")
    public void onRestoreState(@Nullable Bundle bundle) {
        if (bundle != null) {
            ArrayList<TabInfoAndroid> parcelables = bundle.getParcelableArrayList(TABS_KEY);
            if (parcelables != null) {
                mTabs = parcelables;
            }
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return getTab(position).getTabName();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        Fragment fragment = (Fragment) object;
        Class<? extends Fragment> fragmentClass = fragment.getClass();
        int index = 0;
        for (TabInfoAndroid tabInfo : mTabs) {
            if (tabInfo.getTabClassName().equals(fragmentClass.getName())) {
                return index;
            }
            ++index;
        }
        return POSITION_NONE;
    }

    @SuppressWarnings("UnusedReturnValue")
    boolean setTabs(List<TabInfoAndroid> tabs) {
        if (!mTabs.equals(tabs)) {
            mTabs.clear();
            mTabs.addAll(tabs);
            notifyDataSetChanged();
            return true;
        }
        return false;
    }

    @SuppressWarnings("UnusedReturnValue")
    boolean setTab(TabInfoAndroid tab) {
        if (mTabs.size() == 1 && mTabs.get(0).equals(tab)) {
            return false;
        }

        mTabs.clear();
        mTabs.add(tab);
        notifyDataSetChanged();
        return true;
    }
}

