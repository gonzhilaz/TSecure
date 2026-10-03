/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.appcontrol.model;

public enum AppControlMode {
    BothLists(0),
    AllowList(1),
    BlockList(2);

    private int mId;

    AppControlMode(int id) {
        mId = id;
    }
    public int getId() {
        return mId;
    }
}