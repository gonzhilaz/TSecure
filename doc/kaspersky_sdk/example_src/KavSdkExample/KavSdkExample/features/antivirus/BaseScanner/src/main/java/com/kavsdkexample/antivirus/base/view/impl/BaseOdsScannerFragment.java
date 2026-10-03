/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.view.impl;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.DocumentsProvider;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.appcompat.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;
import android.net.Uri;

import com.kavsdkexample.antivirus.base.model.ScanObjectsType;
import com.kavsdkexample.antivirus.base.model.ScanObserver;
import com.kavsdkexample.antivirus.base.model.ScanResults;
import com.kavsdkexample.antivirus.base.model.AvAction;
import com.kavsdkexample.antivirus.base.presenter.BaseOdsScannerPresenter;
import com.kavsdkexample.antivirus.base.scanner.R;
import com.kavsdkexample.antivirus.base.scanner.R2;
import com.kavsdkexample.antivirus.base.view.OdsScannerBaseView;
import com.kavsdkexample.core.app.utils.PermissionUtils;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.core.app.view.impl.BaseDialogFragment;
import com.kavsdkexample.core.app.view.impl.DirectoryChooserFragment;
import com.kavsdkexample.core.app.view.impl.DirectoryDocumentChooserFragment;
import com.kavsdkexample.core.app.view.impl.OnDialogFragmentResultListener;
import com.kavsdkexample.core.app.view.impl.nonmvp.ApplicationChooserFragment;
import com.kavsdkexample.core.app.view.utils.UiUtils;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.Unbinder;

public abstract class BaseOdsScannerFragment<SCANRESULTS extends ScanResults, VIEW extends OdsScannerBaseView<SCANRESULTS>>
                             extends AntivirusBaseFragment<VIEW,
                                                           BaseViewState,
                                                           BaseOdsScannerPresenter<VIEW,
                                                                                   BaseViewState,
                                                                                   SCANRESULTS>>
                             implements OdsScannerBaseView<SCANRESULTS>,
                                        RadioGroup.OnCheckedChangeListener,
                                        CompoundButton.OnCheckedChangeListener,
                                        OnClickListener,
                                        View.OnTouchListener,
                                        OnDialogFragmentResultListener {
    private static final String TAG = BaseOdsScannerFragment.class.getSimpleName();

    private static final int SD_CARD_RADIO_BUTTON_ID_START = 1000;
    private static final int SD_CARD_RADIO_BUTTON_ID_END   = 5000;

    private static final int ALL_FILES_PERMISSION_REQ_CODE = 10;

    @BindView(R2.id.RadioGroupActionIfNotCured) RadioGroup   mScanActionRadioGroup;
    @BindView(R2.id.ObjectsToScan)              RadioGroup   mObjectsToScan;
    @BindView(R2.id.StartScan)                  View         mStartScanCommandView;
    @BindView(R2.id.PauseScan)                  View         mPauseScanCommandView;
    @BindView(R2.id.CloudScanOnly)              SwitchCompat mCloudOnlyScanSwitch;
    @BindView(R2.id.ScanUds)                    SwitchCompat mAllowCloudScanSwitch;
    @BindView(R2.id.ScanDetectRiskwareAdware)   SwitchCompat mScanRiskwareSwitch;
    @BindView(R2.id.ScanSuspicious)             SwitchCompat mScanSuspiciousSwitch;
    @BindView(R2.id.ScanTryCure)                SwitchCompat mScanTryCureSwitch;
    @BindView(R2.id.ScanSelectedDirectoryDocument) RadioButton mScanSelectedDirectoryDocument;
    @BindView(R2.id.ScanSelectedDocument)       RadioButton mScanSelectedDocument;


    private Unbinder mUnbinder;

    private TextView mStartScanCommandCaptionView;
    private TextView mPauseScanCommandCaptionView;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.base_ods_scanner_fragment, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mScanActionRadioGroup.setOnCheckedChangeListener(this);
        mObjectsToScan.setOnCheckedChangeListener(this);

        mStartScanCommandCaptionView = mStartScanCommandView.findViewById(R.id.commandCaption);
        mPauseScanCommandCaptionView = mPauseScanCommandView.findViewById(R.id.commandCaption);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mScanSelectedDirectoryDocument.setEnabled(false);
            mScanSelectedDocument.setEnabled(false);
        }

        return view;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group == mScanActionRadioGroup) {
            getPresenter().setAvAction(ScanUiUtils.resIdToAvAction(checkedId));
        } else if (group == mObjectsToScan) {
            ScanObjectsType scanObjectsType = resIdToScanObjectsType(checkedId);
            if (scanObjectsType == ScanObjectsType.File ||
                scanObjectsType == ScanObjectsType.Folder ||
                scanObjectsType == ScanObjectsType.DirectoryDocument ||
                scanObjectsType == ScanObjectsType.Document ||
                scanObjectsType == ScanObjectsType.SingleInstalledApp){
                return;
            }
            String path = getTagStringForView(group, checkedId);
            getPresenter().setObjectsToScan(resIdToScanObjectsType(checkedId), path, true);
            if (scanObjectsType == ScanObjectsType.SdCard) {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                    DirectoryDocumentChooserFragment.show(
                            requireActivity(),
                            path,
                            DirectoryDocumentChooserFragment.SDCARD_MODE,
                            DirectoryDocumentChooserFragment.SDCARD_MODE,
                            getFragmentTag()
                    );
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                                Uri.parse("package:" + requireContext().getPackageName()));
                        startActivityForResult(intent, ALL_FILES_PERMISSION_REQ_CODE);
                    } catch (ActivityNotFoundException ignore) {
                    }
                }
            }
        }
    }

    @Override
    public void setObjectsToScan(@NonNull ScanObjectsType scanObjectsType, @NonNull String path) {
        int resId = scanObjectsTypeToResId(scanObjectsType);
        if (resId >= SD_CARD_RADIO_BUTTON_ID_START && resId <= SD_CARD_RADIO_BUTTON_ID_END) {
            int childsCount = mObjectsToScan.getChildCount();
            for (int i = 0; i < childsCount; ++i) {
                View currentView = mObjectsToScan.getChildAt(i);
                Object tag = currentView.getTag();
                String radioButtonTagPath = tag == null ? "" : (String) tag;
                if (radioButtonTagPath.equals(path)) {
                    resId = currentView.getId();
                }
            }
        }
        updateObjectToScanGroupLabel(resId, path);
        if (resId != R.id.ScanSelectedFolder) {
            updateObjectToScanGroupLabel(R.id.ScanSelectedFolder, "");
        }
        if (resId != R.id.ScanFile) {
            updateObjectToScanGroupLabel(R.id.ScanFile, "");
        }
        if (resId != R.id.ScanInstalledAppRadio) {
            updateObjectToScanGroupLabel(R.id.ScanInstalledAppRadio, "");
        }
        if (resId != R.id.ScanSelectedDirectoryDocument) {
            updateObjectToScanGroupLabel(R.id.ScanSelectedDirectoryDocument, "");
        }
        if (resId != R.id.ScanSelectedDocument) {
            updateObjectToScanGroupLabel(R.id.ScanSelectedDocument, "");
        }

        UiUtils.silentRadioCheck(mObjectsToScan, resId, this);
    }

    @Override
    public void setCloudOnlyScan(boolean enabled) {
        mAllowCloudScanSwitch.setEnabled(!enabled);
        UiUtils.setCheckedSilent(mCloudOnlyScanSwitch, enabled, this);
    }

    @Override
    public void setAllowCloudScan(boolean enabled) {
        UiUtils.setCheckedSilent(mAllowCloudScanSwitch, enabled, this);
    }

    @Override
    public void setScanSuspicious(boolean enabled) {
        UiUtils.setCheckedSilent(mScanSuspiciousSwitch, enabled, this);
    }

    @Override
    public void setDetectRiskwareAdware(boolean enabled) {
        UiUtils.setCheckedSilent(mScanRiskwareSwitch, enabled, this);
    }

    @Override
    @OnCheckedChanged({ R2.id.CloudScanOnly,
                        R2.id.ScanUds,
                        R2.id.ScanDetectRiskwareAdware,
                        R2.id.ScanSuspicious })
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();

        BaseOdsScannerPresenter<
            VIEW,
            BaseViewState,
            SCANRESULTS
        > presenter = getPresenter();
        String scanCheckboxLogString;
        if (id == R.id.CloudScanOnly) {
            mAllowCloudScanSwitch.setEnabled(!isChecked);
            presenter.setCloudOnlyScan(isChecked);
            scanCheckboxLogString = "'Scan: cloud scan is ";
        } else if (id == R.id.ScanUds) {
            presenter.setAllowCloudScan(isChecked);
            scanCheckboxLogString = "'Scan: check in cloud' is ";
        } else if (id == R.id.ScanDetectRiskwareAdware) {
            presenter.setScanRiskware(isChecked);
            scanCheckboxLogString = "'Scan: detect riskware adware' is ";
        } else if (id == R.id.ScanSuspicious) {
            presenter.setScanSuspicious(isChecked);
            scanCheckboxLogString = "'Scan: suspicious' is ";
        } else {
            scanCheckboxLogString = null;
        }
        if (scanCheckboxLogString != null) {
            Log.d(TAG, scanCheckboxLogString + (isChecked ? "enabled" : "disabled"));
        }
    }

    @NonNull
    protected SwitchCompat getTryCureSwitch() {
        return mScanTryCureSwitch;
    }

    @Override
    public void showSdCardsOptions(@NonNull List<String> sdCardPaths) {
        int i = 1;
        for (final String path : sdCardPaths) {
            RadioButton radioButton = (RadioButton) getLayoutInflater().inflate(R.layout.radiobutton_template, null);
            radioButton.setText(String.format(getString(R.string.str_antivirus_sd_card_name), i, path));
            radioButton.setTag(path);
            radioButton.setId(SD_CARD_RADIO_BUTTON_ID_START + i - 1);
            radioButton.setSaveEnabled(false);
            mObjectsToScan.addView(radioButton);
            i++;
        }
    }

    @NonNull
    private static ScanObjectsType resIdToScanObjectsType(int id) {
        if (id == R.id.ScanInstalledAppsRadio) {
            return ScanObjectsType.AllInstalledApps;
        } else if (id == R.id.ScanInstalledAppRadio) {
            return ScanObjectsType.SingleInstalledApp;
        } else if (id == R.id.ScanAll) {
            return ScanObjectsType.AllFiles;
        } else if (id == R.id.ScanMemory) {
            return ScanObjectsType.DeviceInternalStorage;
        } else if (id == R.id.ScanSelectedFolder) {
            return ScanObjectsType.Folder;
        } else if (id == R.id.ScanFile) {
            return ScanObjectsType.File;
        } else if (id >= SD_CARD_RADIO_BUTTON_ID_START && id <= SD_CARD_RADIO_BUTTON_ID_END) {
            return ScanObjectsType.SdCard;
        } else if (id == R.id.ScanSelectedDirectoryDocument) {
            return ScanObjectsType.DirectoryDocument;
        } else if (id == R.id.ScanSelectedDocument) {
            return ScanObjectsType.Document;
        } else {
            throw new IllegalStateException("Unknown ScanObjectsType resource id: " + id);
        }
    }

    private static int scanObjectsTypeToResId(@NonNull ScanObjectsType scanObjectsType) {
        switch (scanObjectsType) {
            case AllInstalledApps:
                return R.id.ScanInstalledAppsRadio;
            case SingleInstalledApp:
                return R.id.ScanInstalledAppRadio;
            case AllFiles:
                return R.id.ScanAll;
            case DeviceInternalStorage:
                return R.id.ScanMemory;
            case Folder:
                return R.id.ScanSelectedFolder;
            case File:
                return R.id.ScanFile;
            case SdCard:
                return SD_CARD_RADIO_BUTTON_ID_START;
            case DirectoryDocument:
                return R.id.ScanSelectedDirectoryDocument;
            case Document:
                return R.id.ScanSelectedDocument;
            default:
                throw new IllegalStateException("Unknown ScanObjectsType: " + scanObjectsType);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void setAvAction(@NonNull AvAction action) {
        UiUtils.silentRadioCheck(mScanActionRadioGroup, ScanUiUtils.avActionToResId(action), this);
    }

    @Override
    public void setScanButtonState(@NonNull ScanButtonState state) {
        ScanUiUtils.setScanButtonState(state, mStartScanCommandCaptionView, mStartScanCommandView, mPauseScanCommandView);
    }

    @Override
    public void setPauseButtonState(@NonNull PauseButtonState state) {
        ScanUiUtils.setPauseButtonState(state, mPauseScanCommandCaptionView, mPauseScanCommandView);
    }

    @Override
    public void showError(@NonNull ScanObserver.ScanErrorType error) {
        String errorText;
        switch (error) {
            case PathNotExists:
                errorText = getString(R.string.str_antivirus_path_not_exist, getPresenter().getCurrentScanPath());
                break;
            case PathIsNotFile:
                errorText = getString(R.string.str_antivirus_path_not_file, getPresenter().getCurrentScanPath());
                break;
            case PathIsNotFolder:
                errorText = getString(R.string.str_antivirus_path_not_folder, getPresenter().getCurrentScanPath());
                break;
            case LicenseError:
                errorText = getString(R.string.str_antivirus_license_error);
                break;
            default:
                errorText = getString(R.string.str_antivirus_unknown_error);
        }
        Toast.makeText(requireContext(), errorText, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showScanResults(@NonNull ScanResults scanResults) {
    }

    @Override
    @OnClick({ R2.id.StartScan,
               R2.id.PauseScan,
               R2.id.ScanSelectedFolder,
               R2.id.ScanFile,
               R2.id.ScanInstalledAppRadio,
               R2.id.ScanSelectedDirectoryDocument,
               R2.id.ScanSelectedDocument })
    public void onClick(View v) {
        int id = v.getId();
        BaseOdsScannerPresenter<
            VIEW,
            BaseViewState,
            SCANRESULTS
        > presenter = getPresenter();

        if (id == R.id.StartScan) {
            presenter.processScanButtonClick();
        } else if (id == R.id.PauseScan) {
            presenter.processPauseResumeButtonClick();
        } else if (id == R.id.ScanSelectedFolder ||
                   id == R.id.ScanFile ||
                   id == R.id.ScanInstalledAppRadio ||
                   id == R.id.ScanSelectedDirectoryDocument ||
                   id == R.id.ScanSelectedDocument) {
            Object tag = v.getTag();
            String path = tag instanceof String ? (String) tag : "";
            getPresenter().setObjectsToScan(resIdToScanObjectsType(id), path, true);
        }
    }

    @NonNull
    private static String getTagStringForView(@NonNull ViewGroup group, int viewId) {
        Object tag = group.findViewById(viewId).getTag();
        if (tag instanceof String) {
            return (String) tag;
        }
        return "";
    }

    @Override
    public void selectApplicationToScan() {
        ApplicationChooserFragment.show(requireActivity(), true, true, getFragmentTag());
    }

    @Override
    public void selectFileToScan(@NonNull String currentFile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
            SelectStorageDialogFragment.show(
                    requireActivity(),
                    DirectoryChooserFragment.FILE_MODE,
                    getFragmentTag()
            );
        } else {
            File file = currentFile.isEmpty() ? requireContext().getFilesDir() : new File(currentFile).getParentFile();
            DirectoryChooserFragment.show(
                    requireActivity(),
                    file.getParentFile(),
                    DirectoryChooserFragment.FILE_MODE,
                    DirectoryChooserFragment.FILE_MODE,
                    getFragmentTag()
            );
        }
    }

    @Override
    public void selectFolderToScan(@NonNull String currentFolder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
            SelectStorageDialogFragment.show(
                    requireActivity(),
                    DirectoryChooserFragment.DIRECTORY_MODE,
                    getFragmentTag()
            );
        } else {
            File initDir = currentFolder.isEmpty() ? requireContext().getFilesDir() : new File(currentFolder);
            DirectoryChooserFragment.show(
                    requireActivity(),
                    initDir,
                    DirectoryChooserFragment.DIRECTORY_MODE,
                    DirectoryChooserFragment.DIRECTORY_MODE,
                    getFragmentTag()
            );
        }
    }

    @Override
    public void selectDocumentToScan(@NonNull String currentDocument) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        Uri uri = Uri.parse(currentDocument);
        if (uri != null) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
        }
        startActivityForResult(intent, DirectoryDocumentChooserFragment.FILE_MODE);
    }

    @Override
    public void selectDirectoryDocumentToScan(@NonNull String currentDirectoryDocument) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        Uri uri = Uri.parse(currentDirectoryDocument);
        if (uri != null) {
            String treeDocumentId = PermissionUtils.getTreeDocumentId(uri);
            if (treeDocumentId != null) {
                uri = DocumentsContract.buildRootUri(uri.getAuthority(), treeDocumentId);
            }
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
        }
        startActivityForResult(intent, DirectoryDocumentChooserFragment.DIRECTORY_MODE);
    }

    private void updateObjectToScanGroupLabel(int id, @NonNull String path) {
        if (id == R.id.ScanSelectedFolder) {
            RadioButton scanFolderRadioBtn = mObjectsToScan.findViewById(R.id.ScanSelectedFolder);
            scanFolderRadioBtn.setText(getString(R.string.str_antivirus_scan_selected_folder, path));
            scanFolderRadioBtn.setTag(path);
        } else if (id == R.id.ScanFile) {
            RadioButton scanFileRadioBtn = mObjectsToScan.findViewById(R.id.ScanFile);
            scanFileRadioBtn.setText(getString(R.string.str_antivirus_scan_file, path));
            scanFileRadioBtn.setTag(path);
        } else if (id == R.id.ScanInstalledAppRadio) {
            RadioButton scanAppRadioBtn = mObjectsToScan.findViewById(R.id.ScanInstalledAppRadio);
            scanAppRadioBtn.setText(getString(R.string.str_antivirus_scan_installed_application, path));
            scanAppRadioBtn.setTag(path);
        } else if (id == R.id.ScanSelectedDirectoryDocument) {
            RadioButton scanFolderRadioBtn = mObjectsToScan.findViewById(R.id.ScanSelectedDirectoryDocument);
            String documentid = "";
            if (path != null) {
                documentid = Uri.parse(path).getLastPathSegment();
                if (documentid == null) {
                    documentid = "";
                }
            }
            scanFolderRadioBtn.setText(getString(R.string.str_antivirus_scan_selected_directory_document, documentid));
            scanFolderRadioBtn.setTag(path);
        } else if (id == R.id.ScanSelectedDocument) {
            RadioButton scanFolderRadioBtn = mObjectsToScan.findViewById(R.id.ScanSelectedDocument);
            String documentid = "";
            if (path != null) {
                documentid = Uri.parse(path).getLastPathSegment();
                if (documentid == null) {
                    documentid = "";
                }
            }
            scanFolderRadioBtn.setText(getString(R.string.str_antivirus_scan_selected_document, documentid));
            scanFolderRadioBtn.setTag(path);
        }
    }

    @Override
    public void onDialogFragmentResult(BaseDialogFragment dialog, Object data) {
        if (dialog instanceof DirectoryChooserFragment) {
            String path = ((File) data).getAbsolutePath();
            switch (dialog.getRequestId()) {
                case DirectoryChooserFragment.DIRECTORY_MODE:
                    File folder = (File) data;
                    getPresenter().setObjectsToScan(ScanObjectsType.Folder, folder.getAbsolutePath(), false);
                    updateObjectToScanGroupLabel(R.id.ScanSelectedFolder, path);
                    break;
                case DirectoryChooserFragment.FILE_MODE:
                    File file = (File) data;
                    getPresenter().setObjectsToScan(ScanObjectsType.File, file.getAbsolutePath(), false);
                    updateObjectToScanGroupLabel(R.id.ScanFile, path);
                    break;
                default:
                    break;
            }
        } else if (dialog instanceof ApplicationChooserFragment) {
            String appPackage = data.toString();
            getPresenter().setObjectsToScan(ScanObjectsType.SingleInstalledApp, appPackage, false);
            updateObjectToScanGroupLabel(R.id.ScanInstalledAppRadio, appPackage);
        } else if (dialog instanceof DirectoryDocumentChooserFragment) {
            switch (dialog.getRequestId()) {
                case DirectoryDocumentChooserFragment.DIRECTORY_MODE:
                    getPresenter().setObjectsToScan(ScanObjectsType.DirectoryDocument, (String) data, false);
                    updateObjectToScanGroupLabel(R.id.ScanSelectedDirectoryDocument, (String) data);
                    break;
                case DirectoryDocumentChooserFragment.FILE_MODE:
                    getPresenter().setObjectsToScan(ScanObjectsType.Document, (String) data, false);
                    updateObjectToScanGroupLabel(R.id.ScanSelectedDocument, (String) data);
                    break;
                default:
                    break;
            }
        } else if (dialog instanceof SelectStorageDialogFragment) {
            SelectStorageDialogFragment.Result result = (SelectStorageDialogFragment.Result) data;
            int requestId = dialog.getRequestId();
            if (result.mDefaultFileSystem) {
                openDefaultFolder(requestId);
            } else {
                File initDir = result.mExtStorageVolume.getPathFile();
                if (initDir != null) {
                    DirectoryChooserFragment.show(
                            requireActivity(),
                            initDir,
                            requestId,
                            requestId,
                            getFragmentTag()
                    );
                } else {
                    Log.e(TAG, "Failed to open an external storage");
                    openDefaultFolder(requestId);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if (uri == null) {
                return;
            }
            requireActivity().getContentResolver().takePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            switch (requestCode) {
                case DirectoryDocumentChooserFragment.DIRECTORY_MODE:
                    String treeDocumentId = PermissionUtils.getTreeDocumentId(uri);
                    if (treeDocumentId != null) {
                        uri = DocumentsContract.buildDocumentUriUsingTree(uri, treeDocumentId);
                    }
                    getPresenter().setObjectsToScan(ScanObjectsType.DirectoryDocument, uri.toString(), false);
                    updateObjectToScanGroupLabel(R.id.ScanSelectedDirectoryDocument, uri.toString());
                    break;
                case DirectoryDocumentChooserFragment.FILE_MODE:
                    getPresenter().setObjectsToScan(ScanObjectsType.Document, uri.toString(), false);
                    updateObjectToScanGroupLabel(R.id.ScanSelectedDocument, uri.toString());
                    break;
                default:
                    break;
            }
        }
    }

    private void openDefaultFolder(int requestId) {
        File initDir = requireContext().getFilesDir();
        DirectoryChooserFragment.show(
                requireActivity(),
                initDir,
                requestId,
                requestId,
                getFragmentTag()
        );
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_MOVE && view.getParent() != null) {
            view.getParent().requestDisallowInterceptTouchEvent(true);
        }

        view.onTouchEvent(motionEvent);
        return true;
    }
}
