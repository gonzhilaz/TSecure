/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.update.model;

public enum UpdateModelUpdateServerMode {
    Random(0),
    Default(1),
    Specific(2);

    private int mId;

    UpdateModelUpdateServerMode(int id) {
        mId = id;
    }
    public int getId() {
        return mId;
    }
}
