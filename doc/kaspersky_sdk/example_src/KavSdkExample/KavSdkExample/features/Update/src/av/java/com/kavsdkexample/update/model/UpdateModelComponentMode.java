/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.update.model;

public enum UpdateModelComponentMode {
    All(0),
    Antivirus(1);

    private int mId;

    UpdateModelComponentMode(int id) {
        mId = id;
    }
    public int getId() {
        return mId;
    }
}
