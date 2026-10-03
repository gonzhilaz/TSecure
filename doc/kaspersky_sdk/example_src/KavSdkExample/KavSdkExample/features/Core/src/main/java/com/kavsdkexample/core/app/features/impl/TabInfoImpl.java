/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.features.impl;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.core.app.features.TabInfoAndroid;

public final class TabInfoImpl implements TabInfoAndroid {
    public static final Parcelable.Creator<TabInfoImpl> CREATOR = new Parcelable.Creator<TabInfoImpl>(){

        @Override
        public TabInfoImpl createFromParcel(Parcel parcel) {
            return new TabInfoImpl(parcel);
        }

        @Override
        public TabInfoImpl[] newArray(int size) {
            return new TabInfoImpl[0];
        }
    };

    private static final int SEED  = 11;
    private static final int PRIME = 17;
    private final String mTabName;
    private final String mTabClassName;


    TabInfoImpl(@NonNull String tabName, @NonNull String tabClassName) {
        mTabName      = tabName;
        mTabClassName = tabClassName;
    }

    private TabInfoImpl(Parcel parcel) {
        mTabName      = parcel.readString();
        mTabClassName = parcel.readString();
    }

    @Override
    @NonNull
    public String getTabName() {
        return mTabName;
    }

    @Override
    @NonNull
    public String getTabClassName() {
        return mTabClassName;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof TabInfoImpl) {
            TabInfoImpl tab = (TabInfoImpl) obj;
            return tab.mTabName.equals(mTabName) &&
                   tab.mTabClassName.equals(mTabClassName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = SEED;
        hash += PRIME * hash + mTabName.hashCode();
        hash += PRIME * hash + mTabClassName.hashCode();
        return hash;
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mTabName);
        parcel.writeString(mTabClassName);
    }
}
