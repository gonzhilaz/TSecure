/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.simpleurlreputation.model;

import com.kaspersky.components.urlchecker.UrlInfo;

public interface UrlReputationListener {
    void onSuccess(UrlInfo result);
    void onFailure(String errorMessage);
}
