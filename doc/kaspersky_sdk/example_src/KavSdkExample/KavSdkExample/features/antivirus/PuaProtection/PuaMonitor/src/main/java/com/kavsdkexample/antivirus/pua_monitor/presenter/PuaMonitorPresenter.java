/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.pua_monitor.presenter;

import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.antivirus.base.monitor.presenter.MonitorBasePresenter;
import com.kavsdkexample.antivirus.pua_monitor.view.PuaMonitorView;

public interface PuaMonitorPresenter extends MonitorBasePresenter<PuaMonitorView, BaseViewState> {
    void enableMissedAppCheck(boolean value);
}
