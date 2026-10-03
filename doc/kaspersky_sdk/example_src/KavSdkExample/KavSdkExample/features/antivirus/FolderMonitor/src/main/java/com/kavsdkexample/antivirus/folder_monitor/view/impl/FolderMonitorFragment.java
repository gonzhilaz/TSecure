/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.folder_monitor.view.impl;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.kavsdkexample.antivirus.base.view.impl.AntivirusBaseFragment;
import com.kavsdkexample.antivirus.base.view.impl.SelectStorageDialogFragment;
import com.kavsdkexample.antivirus.folder_monitor.R;
import com.kavsdkexample.antivirus.folder_monitor.R2;
import com.kavsdkexample.antivirus.folder_monitor.presenter.FolderMonitorPresenter;
import com.kavsdkexample.antivirus.folder_monitor.presenter.FoldersActionType;
import com.kavsdkexample.antivirus.folder_monitor.view.FolderMonitorView;
import com.kavsdkexample.core.app.utils.PermissionUtils;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.core.app.view.impl.BaseDialogFragment;
import com.kavsdkexample.core.app.view.impl.DirectoryChooserFragment;
import com.kavsdkexample.core.app.view.impl.DirectoryDocumentChooserFragment;
import com.kavsdkexample.core.app.view.impl.OnDialogFragmentResultListener;
import com.kavsdkexample.core.app.view.utils.UiUtils;

import java.io.File;
import java.util.Set;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;

/**
 * Example of using the anti-virus to automatically check when add or change the files in a
 * directory
 */
public class FolderMonitorFragment extends    AntivirusBaseFragment<FolderMonitorView,
                                                                    BaseViewState,
                                                                    FolderMonitorPresenter>
                                   implements FolderMonitorView,
                                              View.OnClickListener,
                                              OnDialogFragmentResultListener,
                                              AdapterView.OnItemClickListener {
    private static final String TAG         = FolderMonitorFragment.class.getSimpleName();
    private static final String FRAGMENT_ID = "FolderMonitor";

    private static final int REQUEST_ADD_FOLDER = 0;

    @Inject                            FolderMonitorPresenter  mPresenter;
    @BindView(R2.id.lstFolderMonitors) ListView                mList;
    @BindView(R2.id.empty_list_view)   TextView                mEmptyView;

    private Unbinder             mUnbinder;
    private HeaderViewHolder     mHeaderViewHolder;
    private ArrayAdapter<String> mAdapter;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.folder_monitor_fragment, container, false);
        AndroidSupportInjection.inject(this);
        mUnbinder          = ButterKnife.bind(this, view);
        mAdapter           = new ArrayAdapter<>(requireContext(), R.layout.simple_list_item);
        View controlsView  = inflater.inflate(R.layout.folder_monitor_fragment_header, mList, false);
        mHeaderViewHolder  = new HeaderViewHolder(mPresenter, controlsView);
        mList.addHeaderView(controlsView);
        return view;
    }

    @Override
    public String getFragmentTag() {
        return FRAGMENT_ID;
    }

    @Override
    public boolean onBackPressed() {
        requireFragmentManager().popBackStack();
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHeaderViewHolder.unbind();
        mUnbinder.unbind();
    }

    @Override
    @OnClick({ R2.id.btnAddFolder,
               R2.id.btnClearAll })
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnAddFolder) {
            mPresenter.addButtonClicked();
        } else if (id == R.id.btnClearAll) {
            mPresenter.clearButtonClicked();
        }
    }

    @Override
    @OnItemClick({ R2.id.lstFolderMonitors })
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int viewId = parent.getId();
        if (viewId == R.id.lstFolderMonitors) {
            mPresenter.folderItemClicked((String) parent.getAdapter().getItem(position));
        }
    }

    @Override
    public void setTryCure(boolean enabled) {
        mHeaderViewHolder.setTryCure(enabled);
    }

    @Override
    public void setFoldersToMonitor(@NonNull Set<String> folders) {
        mAdapter.clear();
        mAdapter.addAll(folders);
        mList.setAdapter(mAdapter);
        mEmptyView.setVisibility(folders.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setMonitorEnabledState(boolean enabled) {
    }


    @Override
    public void setAllowCloudCheckState(boolean enabled) {
        mHeaderViewHolder.setAllowCloudCheckState(enabled);
    }

    @Override
    public void setCloudOnlyCheckState(boolean enabled) {
        mHeaderViewHolder.setCloudOnlyCheckState(enabled);
    }

    @Override
    public void setRiskwareCheckState(boolean enabled) {
        mHeaderViewHolder.setRiskwareCheckState(enabled);
    }

    @Override
    public void setMaxFileCheckSize(long size) {
        mHeaderViewHolder.setMaxFileCheckSize(size);
    }

    @Override
    public void onMonitorStateChanged(boolean enabled) {
        // do nothing
    }

    @Override
    public void onDialogFragmentResult(BaseDialogFragment dialog, Object data) {
        if (dialog instanceof DirectoryChooserFragment) {
            File dir = (File) data;
            Log.d(TAG, "selected dir: " + dir.getAbsolutePath());
            mPresenter.addFolder(dir.getAbsolutePath());
        } else if (dialog instanceof DirectoryDocumentChooserFragment) {
            Log.d(TAG, "selected dir: " + data);
            mPresenter.addFolder((String) data);
        } else if (dialog instanceof SelectStorageDialogFragment) {
            SelectStorageDialogFragment.Result result = (SelectStorageDialogFragment.Result) data;
            if (result.mDefaultFileSystem) {
                openDefaultFolder();
            } else {
                try {
                    File initDir = result.mExtStorageVolume.getPathFile();
                    if (initDir != null
                           && (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                              || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                                  && Environment.isExternalStorageManager(initDir)))) {
                        DirectoryChooserFragment.show(
                                requireActivity(),
                                initDir,
                                DirectoryChooserFragment.DIRECTORY_MODE,
                                REQUEST_ADD_FOLDER,
                                FRAGMENT_ID
                        );
                    } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                        DirectoryDocumentChooserFragment.show(
                                requireActivity(),
                                DocumentsContract.buildTreeDocumentUri("com.android.externalstorage.documents", result.mExtStorageVolume.getName()).toString(),
                                DirectoryChooserFragment.DIRECTORY_MODE,
                                REQUEST_ADD_FOLDER,
                                FRAGMENT_ID
                        );
                    } else { //Android R higher and no MANAGE_EXTERNAL_STORAGE permission
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        Uri uri = result.mExtStorageVolume.getRootUri();
                        if (uri != null) {
                            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
                        }
                        startActivityForResult(intent, REQUEST_ADD_FOLDER);
                    }

                } catch (NullPointerException e) {
                    Log.e(TAG, "Failed to open an external storage", e);
                    openDefaultFolder();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_FOLDER && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if (uri == null) {
                return;
            }
            requireActivity().getContentResolver().takePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            String treeDocumentId = PermissionUtils.getTreeDocumentId(uri);
            if (treeDocumentId != null) {
                uri = DocumentsContract.buildDocumentUriUsingTree(uri, treeDocumentId);
            }
            mPresenter.addFolder(uri.toString());
        }
    }

    private void openDefaultFolder() {
        Context context = requireContext();
        File initDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (initDir == null) {
            initDir = context.getFilesDir();
        }
        DirectoryChooserFragment.show(
                requireActivity(),
                initDir,
                DirectoryChooserFragment.DIRECTORY_MODE,
                REQUEST_ADD_FOLDER,
                FRAGMENT_ID
        );
    }

    @NonNull
    @Override
    protected FolderMonitorPresenter getPresenter() {
        return mPresenter;
    }

    @Nullable
    @Override
    protected BaseViewState getViewState() {
        return null;
    }

    @SuppressLint("NewApi")
    @Override
    public void showAddFolderFragment() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            SelectStorageDialogFragment.show(requireActivity(), getFragmentTag());
        } else {
            Context context = requireContext();
            File initDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (initDir == null) {
                initDir = context.getFilesDir();
            }
            DirectoryChooserFragment.show(requireActivity(), initDir, DirectoryChooserFragment.DIRECTORY_MODE, REQUEST_ADD_FOLDER, FRAGMENT_ID);
        }
    }

    @Override
    public void showDeleteFolderFragment(@NonNull String path) {
        FoldersActionDialogFragment.show(requireFragmentManager(), FoldersActionType.Remove, path);
    }

    @Override
    public void showClearAllFragment() {
        FoldersActionDialogFragment.show(requireFragmentManager(), FoldersActionType.Clean, null);
    }

    @Override
    public void showAddedFolder(@NonNull String folder) {
        if (mAdapter != null) {
            mAdapter.add(folder);
            mEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void showRemovedFolder(@NonNull String folder) {
        if (mAdapter != null) {
            mAdapter.remove(folder);
            mEmptyView.setVisibility(mAdapter.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    static class HeaderViewHolder implements CompoundButton.OnCheckedChangeListener,
                                             TextWatcher {
        @BindView(R2.id.cbScanUdsOnly)     SwitchCompat            mScanUdsOnlyCheckBox;
        @BindView(R2.id.cbScanUdsAllow)    SwitchCompat            mScanUdsCheckBox;
        @BindView(R2.id.cbDetectRiskware)  SwitchCompat            mDetectRiskwareCheckbox;
        @BindView(R2.id.cbCureInfected)    SwitchCompat            mCureInfectedCheckbox;
        @BindView(R2.id.etMaxFileSize)     EditText                mMaxFileSizeEditText;

        private FolderMonitorPresenter mPresenter;
        private Unbinder               mUnbinder;


        HeaderViewHolder(@NonNull FolderMonitorPresenter presenter, @NonNull View view) {
            mPresenter = presenter;
            mUnbinder = ButterKnife.bind(this, view);
            mMaxFileSizeEditText.addTextChangedListener(this);
        }

        void unbind() {
            mUnbinder.unbind();
        }

        @Override
        @OnCheckedChanged({ R2.id.cbScanUdsOnly,
                            R2.id.cbScanUdsAllow,
                            R2.id.cbDetectRiskware,
                            R2.id.cbCureInfected })
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int id = buttonView.getId();
            if (id == R.id.cbScanUdsOnly) {
                mPresenter.enableCloudOnlyCheck(isChecked);
            } else if (id == R.id.cbScanUdsAllow) {
                mPresenter.enableCloudCheck(isChecked);
            } else if (id == R.id.cbDetectRiskware) {
                mPresenter.enableRiskwareCheck(isChecked);
            } else if (id == R.id.cbCureInfected) {
                mPresenter.enableTryCure(isChecked);
            }
        }

        void setTryCure(boolean enabled) {
            UiUtils.setCheckedSilent(mCureInfectedCheckbox, enabled, this);
        }

        void setAllowCloudCheckState(boolean enabled) {
            UiUtils.setCheckedSilent(mScanUdsCheckBox, enabled, this);
        }

        void setCloudOnlyCheckState(boolean enabled) {
            UiUtils.setCheckedSilent(mScanUdsOnlyCheckBox, enabled, this);
        }

        void setRiskwareCheckState(boolean enabled) {
            UiUtils.setCheckedSilent(mDetectRiskwareCheckbox, enabled, this);
        }

        void setMaxFileCheckSize(long size) {
            if (size != 0) {
                UiUtils.setTextSilent(mMaxFileSizeEditText, String.valueOf(size), this);
            } else {
                UiUtils.setTextSilent(mMaxFileSizeEditText, "", this);
            }
        }

        // TextWatcher -->
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // do nothing
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // do nothing
        }

        @Override
        public void afterTextChanged(Editable editable) {
            mPresenter.setMaxFileCheckSize(getLong(editable));
        }
        // <-- TextWatcher

        private static long getLong(Editable editable) {
            try {
                return Long.parseLong(editable.toString());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }
}