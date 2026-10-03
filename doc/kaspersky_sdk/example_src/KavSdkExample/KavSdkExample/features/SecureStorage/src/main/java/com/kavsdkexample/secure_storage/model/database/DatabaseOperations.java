/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.model.database;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

@UiThread
public interface DatabaseOperations {
    /**
     * Open new database. If there is an already opened db, it will be closed first
     * @param path full path to database file.
     * @param password database password, can be empty string if withFingerpring parameter value passed as true
     */
    void openDatabase(@NonNull String path, @NonNull String password);

    /**
     * Get path to current opened database
     * @return null if database it not opened, or path to database otherwise
     */
    @NonNull
    String getDatabaseFilePath();

    /**
     * Creates sample table. Database should be opened.
     */
    void createTestTable();

    /**
     * Close current opened database
     * @param removeAfterClose if set to true will remove database file, after database close
     */
    void closeDatabase(boolean removeAfterClose);

    /**
     * Execute sql request on opened database
     * @param sqlRequest string with sql request
     */
    void executeDatabaseRequest(@NonNull String sqlRequest);
}
