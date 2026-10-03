/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.model.file;

import androidx.annotation.NonNull;

public interface FileOperations {
    /**
     * Read secure file
     * @param path full path to secure file.
     * @param password secure file password, can be empty string if withFingerpring parameter value passed as true
     * @param pageNum use to specify page number in file to read if file size is more than default read page size
     */
    void readFile(@NonNull String path, @NonNull String password, int pageNum);

    /**
     * Write secure file
     * @param path full path to secure file.
     * @param password secure file password, can be empty string if withFingerpring parameter value passed as true
     * @param content content string that should be written
     * @param append specifies should file be rewritten or appended for already existing file
     */
    void writeFile(@NonNull String path, @NonNull String password, String content, boolean append);
}
