/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.self_checker.model;


public interface SelfCheckResultsObserver {
    void onSuccess(boolean isCompromised);
    void onBasesUnavailable();
}
