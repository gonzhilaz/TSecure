/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.features;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.Set;

public final class SettingsHelper {
    private SettingsHelper() {
    }

    /*
     * Methods for saving settings represented by strings
     */
    public static void writeStringPref(@NonNull SharedPreferences prefs, @NonNull String pref, @Nullable String value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(pref, value);
        editor.apply();
    }

    /*
     * Methods for saving settings represented by numbers
     */
    public static void writeIntPref(@NonNull SharedPreferences prefs, @NonNull String pref, int value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(pref, value);
        editor.apply();
    }

    /*
     * Methods for saving settings represented by long numbers
     */
    public static void writeLongPref(@NonNull SharedPreferences prefs, @NonNull String pref, long value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(pref, value);
        editor.apply();
    }

    /*
     * Methods for saving settings represented by boolean values
     */
    public static void writeBooleanPref(@NonNull SharedPreferences prefs, @NonNull String pref, boolean value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(pref, value);
        editor.apply();
    }

    /*
     * Set a set of String values in the preferences editor,
     */
    public static void putStringSet(@NonNull SharedPreferences prefs, @NonNull String pref, @Nullable Set<String> value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(pref, value);
        editor.apply();
    }

    /*
     * Retrieve a set of String values from the preferences.
     */
    @Nullable
    public static Set<String> getStringSet(@NonNull SharedPreferences prefs, @NonNull String pref, Set<String> defValues) {
        Set<String> value = prefs.getStringSet(pref, defValues);
        return value == null ? null : new HashSet<>(value);
    }
}
