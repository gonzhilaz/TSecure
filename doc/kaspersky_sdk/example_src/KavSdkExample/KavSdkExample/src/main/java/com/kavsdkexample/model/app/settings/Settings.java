/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.app.settings;

public interface Settings {
    /**
     * Should this application work without permissions or no
     * @return true if we should not ask for permissions any more or false otherwise
     */
    boolean getWorkWithoutPermissions();

    /**
     * Set work without permissions state
     * @param value true to allow application to work without permissions or false otherwise
     */
    void setWorkWithoutPermissions(boolean value);

    /**
     * Set initial wizard completion status
     * @param value true if wizard completed or false otherwise
     */
    void setWizardCompleted(boolean value);

    /**
     * Get wizard completion status
     * @return true if wizard completed or false otherwise
     */
    boolean getWizardCompleted();
}
