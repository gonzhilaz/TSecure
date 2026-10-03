/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.update.model;

public interface UpdateModelObserver {
    void setController(UpdateController controller);
    void updateIsCompleted();
    void updateIsRunning();
    void executionDetails(UpdateResults result);
    void executionDetails(UpdateResults result, int code);
    void executionDetails(UpdateResults result, final String details);
}