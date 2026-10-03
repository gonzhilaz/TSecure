/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_connectivity.repository.settings;

public interface Settings {
    void    setLoadOnlyTrustedUrlsEnabled(boolean value);
    boolean isLoadOnlyTrustedUrlsEnabled();
}
