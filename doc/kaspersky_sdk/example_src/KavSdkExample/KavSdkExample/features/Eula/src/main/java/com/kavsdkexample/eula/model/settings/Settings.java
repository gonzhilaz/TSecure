/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.eula.model.settings;

public interface Settings {
    /**
     * Does eula accepted for this application or not
     * @return true if eula accepted ore or false otherwise
     */
    boolean isEulaAccepted();

    /**
     * Set eula accepted state
     * @param value true to accept eula or false otherwise
     */
    void setEulaAccepted(boolean value);
}
