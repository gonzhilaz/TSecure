/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.presenter.file;

import androidx.annotation.UiThread;

@UiThread
public interface OverwriteOperation {
    void acceptOverwrite(boolean append);
    void discardOverwrite();
}
