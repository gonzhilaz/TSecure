/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.presenter.impl;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.model.BaseOdsScannerModel;
import com.kavsdkexample.antivirus.base.model.ScanObjectsType;
import com.kavsdkexample.antivirus.base.model.ScanResults;
import com.kavsdkexample.antivirus.base.presenter.BaseOdsScannerPresenter;
import com.kavsdkexample.antivirus.base.view.OdsScannerBaseView;
import com.kavsdkexample.core.app.view.BaseViewState;

import java.util.List;
import android.util.Pair;

public class BaseOdsScannerPresenterImpl<VIEW        extends OdsScannerBaseView<SCANRESULTS>,
                                         VIEWSTATE   extends BaseViewState,
                                         MODEL       extends BaseOdsScannerModel<SCANRESULTS>,
                                         SCANRESULTS extends ScanResults>
                                 extends    BaseScannerPresenterImpl<VIEW, VIEWSTATE, MODEL, SCANRESULTS>
                                 implements BaseOdsScannerPresenter<VIEW, VIEWSTATE, SCANRESULTS> {

    protected BaseOdsScannerPresenterImpl(@NonNull MODEL model) {
        super(model);
    }

    @Override
    public void setObjectsToScan(@NonNull ScanObjectsType objectsToScan, @NonNull String path, boolean radioCheckChanged) {
        mModel.setObjectsToScan(objectsToScan, path);
        if (mView != null && radioCheckChanged) {
            if (objectsToScan == ScanObjectsType.Folder) {
                mView.selectFolderToScan(getScanPath());
            } else if (objectsToScan == ScanObjectsType.File) {
                mView.selectFileToScan(getScanPath());
            } else if (objectsToScan == ScanObjectsType.DirectoryDocument) {
                mView.selectDirectoryDocumentToScan(mModel.getObjectsToScanPath());
            } else if (objectsToScan == ScanObjectsType.Document) {
                mView.selectDocumentToScan(mModel.getObjectsToScanPath());
            } else if (objectsToScan == ScanObjectsType.SingleInstalledApp) {
                mView.selectApplicationToScan();
            }
        }
    }

    @NonNull
    private String getScanPath() {
        String path = mModel.getObjectsToScanPath();
        if (path.isEmpty()) {
          List<String> sdCards =  mModel.getSdCardsOptions();
          return sdCards.isEmpty() ? "" : sdCards.get(0);
        } else {
            return path;
        }
    }

    @Override
    public void setCloudOnlyScan(boolean enabled) {
        mModel.setCloudOnlyScan(enabled);
    }

    @Override
    public void setAllowCloudScan(boolean enabled) {
        mModel.setAllowCloudScan(enabled);
    }

    @Override
    public void setScanRiskware(boolean enabled) {
        mModel.setScanRiskware(enabled);
    }

    @Override
    public void setScanSuspicious(boolean enabled) {
        mModel.setScanSuspicious(enabled);
    }

    @NonNull
    @Override
    public String getCurrentScanPath() {
        return mModel.getObjectsToScanPath();
    }
}
