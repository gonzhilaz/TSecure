/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.view.file.impl;

import android.os.Bundle;
import androidx.annotation.NonNull;

import com.kavsdkexample.secure_storage.view.file.FileViewState;

public final class FileViewHelper {
    private static final String FILE_PATH_KEY       = "file_path";
    private static final String PASSWORD_KEY        = "file_password";
    private static final String FILE_CONTENT_KEY    = "file_content";
    private static final String CURRENT_PAGE_KEY    = "current_page";
    private static final String PAGES_COUNT_KEY     = "pages_count";

    private FileViewHelper() {
    }

    @NonNull
    public static FileViewState fromBundle(@NonNull Bundle bundle) {
        return new FileViewStateImpl(
                bundle.getString (FILE_PATH_KEY       ,    ""),
                bundle.getString (PASSWORD_KEY        ,    ""),
                bundle.getString (FILE_CONTENT_KEY    ,    ""),
                bundle.getInt    (CURRENT_PAGE_KEY    ,     0),
                bundle.getInt    (PAGES_COUNT_KEY     ,     0)
        );
    }

    public static void toBundle(@NonNull Bundle bundle, @NonNull FileViewState state) {
        bundle.putString (FILE_PATH_KEY       , state.getFilePath()            );
        bundle.putString (PASSWORD_KEY        , state.getPassword()            );
        bundle.putString (FILE_CONTENT_KEY    , state.getFileContent()         );
        bundle.putInt    (CURRENT_PAGE_KEY    , state.getCurrentFilePage()     );
        bundle.putInt    (PAGES_COUNT_KEY     , state.getFilePagesCount()      );
    }
}
