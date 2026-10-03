/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.rtp_monitor.view.impl;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;

import com.kavsdk.antivirus.MonitorNotifyConstants;
import com.kavsdkexample.antivirus.base.view.impl.SelectStorageDialogFragment;
import com.kavsdkexample.antivirus.rtp_monitor.R;
import com.kavsdkexample.antivirus.rtp_monitor.R2;
import com.kavsdkexample.antivirus.rtp_monitor.presenter.DirectoryManagerPresenter;
import com.kavsdkexample.antivirus.rtp_monitor.presenter.RemoveType;
import com.kavsdkexample.antivirus.rtp_monitor.view.DirectoryManagerView;
import com.kavsdkexample.core.app.utils.PermissionUtils;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.core.app.view.impl.BaseDialogFragment;
import com.kavsdkexample.core.app.view.impl.DirectoryChooserFragment;
import com.kavsdkexample.core.app.view.impl.DirectoryDocumentChooserFragment;
import com.kavsdkexample.core.app.view.impl.OnDialogFragmentResultListener;
import com.kavsdkexample.core.ui.BaseFragment;

import java.io.File;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;


/**
 * Example of using the anti-virus to automatically check when add or change the files in a
 * directory
 */
public class DirectoryManagerFragment extends   BaseFragment<DirectoryManagerView,
                                                             BaseViewState,
                                                             DirectoryManagerPresenter>
                                               implements DirectoryManagerView,
                                                          OnDialogFragmentResultListener,
                                                          OnClickListener,
                                                          OnItemClickListener {
    private static final String TAG         = DirectoryManagerFragment.class.getSimpleName();
    private static final String FRAGMENT_ID = "DirectoryManagerFragment";

    private static final boolean DEBUG = false;
    private static final int REQUEST_ADD_DIRECTORY = 0;
    private static final int REQUEST_ADD_EXCLUSION = 1;

    @BindView(R2.id.lstMonitoredFolders)   ListView                  mDirectoryList;
    @BindView(R2.id.lstExcludedFolders)    ListView                  mExclusionList;
    @BindView(android.R.id.tabhost)        TabHost                   mTabs;
    @BindView(R2.id.cbNotifyAccess)        SwitchCompat              mChbNotifyAccess;
    @BindView(R2.id.cbNotifyModify)        SwitchCompat              mChbNotifyModify;
    @BindView(R2.id.cbNotifyAttrib)        SwitchCompat              mChbNotifyAttrib;
    @BindView(R2.id.cbNotifyCloseWrite)    SwitchCompat              mChbNotifyCloseWrite;
    @BindView(R2.id.cbNotifyCloseNoWrite)  SwitchCompat              mChbNotifyCloseNoWrite;
    @BindView(R2.id.cbNotifyOpen)          SwitchCompat              mChbNotifyOpen;
    @BindView(R2.id.cbNotifyMovedFrom)     SwitchCompat              mChbNotifyMovedFrom;
    @BindView(R2.id.cbNotifyMovedTo)       SwitchCompat              mChbNotifyMovedTo;
    @BindView(R2.id.cbNotifyCreate)        SwitchCompat              mChbNotifyCreate;
    @BindView(R2.id.cbNotifyDelete)        SwitchCompat              mChbNotifyDelete;
    @BindView(R2.id.cbNotifyMoveSelf)      SwitchCompat              mChbNotifyMoveSelf;
    @BindView(R2.id.cbNotifyDeleteSelf)    SwitchCompat              mChbNotifyDeleteSelf;

    @Inject                                DirectoryManagerPresenter mPresenter;

    private ArrayAdapter<String> mDirectoriesAdapter;
    private ArrayAdapter<String> mExclusionAdapter;


    private Unbinder mUnbinder;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.directory_manager_fragment, container, false);

        AndroidSupportInjection.inject(this);
        mUnbinder = ButterKnife.bind(this, view);

        mDirectoriesAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1);
        mExclusionAdapter   = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1);
        mDirectoryList.setAdapter(mDirectoriesAdapter);
        mExclusionList.setAdapter(mExclusionAdapter);

        @SuppressLint("InflateParams")
        View textForEmptyListView = inflater.inflate(R.layout.text_for_empty_listview, null);
        mDirectoryList.setEmptyView(textForEmptyListView);
        mExclusionList.setEmptyView(textForEmptyListView);

        mTabs.setup();
        setupTab(mTabs, "addDirectoryTab", R.id.addDirectoryTab, getString(R.string.str_av_protection_rtp_monitor_add_directory_tab));
        setupTab(mTabs, "directoriesTab",  R.id.directoriesTab,  getString(R.string.str_av_protection_rtp_monitor_directories_tab));
        setupTab(mTabs, "exclusionsTab",   R.id.exclusionsTab,   getString(R.string.str_av_protection_rtp_monitor_exclusions_tab));
        mTabs.setCurrentTab(0);


        return view;
    }

    @Override
    public String getFragmentTag() {
        return FRAGMENT_ID;
    }

    private static void setupTab(@NonNull TabHost tabHost, @NonNull String tabSpec, int tabId, @NonNull String tabName) {
        TabHost.TabSpec spec = tabHost.newTabSpec(tabSpec);
        spec.setContent(tabId);
        spec.setIndicator(tabName);
        tabHost.addTab(spec);
    }

    @Override
    public boolean onBackPressed() {
        requireFragmentManager().popBackStack();
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    @NonNull
    protected DirectoryManagerPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    @Nullable
    protected BaseViewState getViewState() {
        return null;
    }

    @Override
    public void updateFoldersToMonitor(@NonNull Map<String, Integer> items) {
        mDirectoriesAdapter.clear();
        mDirectoriesAdapter.addAll(items.keySet());
    }

    @Override
    public void updateExcludedFolders(@NonNull Set<String> items) {
        mExclusionAdapter.clear();
        mExclusionAdapter.addAll(items);
    }

    @Override
    public void addMonitoringFolder(@NonNull String folder) {
        mDirectoriesAdapter.add(folder);
    }

    @Override
    public void removeMonitoringFolder(@NonNull String folder) {
        mDirectoriesAdapter.remove(folder);
    }

    @Override
    public void addExclusionFolder(@NonNull String folder) {
        mExclusionAdapter.add(folder);
    }

    @Override
    public void removeExclusionFolder(@NonNull String folder) {
        mExclusionAdapter.remove(folder);
    }

    @Override
    @OnClick({ R2.id.btnAddFolder,
               R2.id.btnAddDefaultFolders,
               R2.id.btnRemoveDefaultFolders,
               R2.id.btnAddExclusionFolder})
    public void onClick(View v) {
        int id = v.getId();
        int requestId = -1;
        if (id == R.id.btnAddFolder) {
            requestId = REQUEST_ADD_DIRECTORY;
        } else if  (id == R.id.btnAddExclusionFolder) {
            requestId = REQUEST_ADD_EXCLUSION;
        } else if (id == R.id.btnAddDefaultFolders) {
            mPresenter.addDefaultDirectories();
        } else if  (id == R.id.btnRemoveDefaultFolders) {
            mPresenter.removeDefaultDirectories();
        }

        if (requestId != -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                SelectStorageDialogFragment.show(requireActivity(), requestId, FRAGMENT_ID);
            } else {
                Context context = requireContext();
                File initDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (initDir == null) {
                    initDir = context.getFilesDir();
                }
                DirectoryChooserFragment.show(requireActivity(),
                        initDir,
                        DirectoryChooserFragment.DIRECTORY_MODE,
                        requestId,
                        FRAGMENT_ID);
            }
        }
    }

    @Override
    public void onDialogFragmentResult(BaseDialogFragment dialog, Object data) {

        if (dialog instanceof DirectoryChooserFragment || dialog instanceof DirectoryDocumentChooserFragment) {
            String dir = data instanceof String ? (String)data : ((File) data).getPath();
            int requestId = dialog.getRequestId();
            if (DEBUG) {
                Log.d(TAG, "selected dir = " + dir);
            }
            if (requestId == REQUEST_ADD_DIRECTORY) {
                mPresenter.addFolderForMonitoring(dir, getNotifyFlags());
            } else if (requestId == REQUEST_ADD_EXCLUSION) {
                mPresenter.addExcludeFolder(dir);
            }
        } else if (dialog instanceof SelectStorageDialogFragment) {
            SelectStorageDialogFragment.Result result = (SelectStorageDialogFragment.Result) data;
            int requestId = dialog.getRequestId();
            if (result.mDefaultFileSystem) {
                openDefaultFolder(requestId);
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
                                requestId,
                                FRAGMENT_ID
                        );
                    } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                        DirectoryDocumentChooserFragment.show(
                                requireActivity(),
                                DocumentsContract.buildTreeDocumentUri("com.android.externalstorage.documents", result.mExtStorageVolume.getName()).toString(),
                                DirectoryChooserFragment.DIRECTORY_MODE,
                                requestId,
                                FRAGMENT_ID
                        );
                    } else { //Android R higher and no MANAGE_EXTERNAL_STORAGE permission
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        Uri uri = result.mExtStorageVolume.getRootUri();
                        if (uri != null) {
                            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
                        }
                        startActivityForResult(intent, requestId);
                    }

                } catch (NullPointerException e) {
                    Log.e(TAG, "Failed to open an external storage", e);
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
            String treeDocumentId = PermissionUtils.getTreeDocumentId(uri);
            if (treeDocumentId != null) {
                uri = DocumentsContract.buildDocumentUriUsingTree(uri, treeDocumentId);
            }
            if (requestCode == REQUEST_ADD_DIRECTORY) {
                mPresenter.addFolderForMonitoring(uri.toString(), getNotifyFlags());
            } else if (requestCode == REQUEST_ADD_EXCLUSION) {
                mPresenter.addExcludeFolder(uri.toString());
            }
        }
    }

    private void openDefaultFolder(int requestId) {
        Context context = requireContext();
        File initDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (initDir == null) {
            initDir = context.getFilesDir();
        }
        DirectoryChooserFragment.show(
                requireActivity(),
                initDir,
                DirectoryChooserFragment.DIRECTORY_MODE,
                requestId,
                FRAGMENT_ID
        );
    }

    @Override
    @OnItemClick({ R2.id.lstMonitoredFolders,
                   R2.id.lstExcludedFolders })
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int viewId = parent.getId();

        if (viewId == R.id.lstMonitoredFolders) {
            String folder = mDirectoriesAdapter.getItem(position);
            if (folder != null) {
                RemoveDialogFragment.show(requireFragmentManager(), RemoveType.FolderToMonitor, folder);
            }
        } else if (viewId == R.id.lstExcludedFolders) {
            String folder = mExclusionAdapter.getItem(position);
            if (folder != null) {
                RemoveDialogFragment.show(requireFragmentManager(), RemoveType.Exclusion, folder);
            }
        }
    }

    private int getNotifyFlags() {
        int flags = 0;
        if (mChbNotifyAccess.isChecked()) {
            flags |= MonitorNotifyConstants.NOTIFY_ACCESS;
        }
        if (mChbNotifyModify.isChecked()) {
            flags |= MonitorNotifyConstants.NOTIFY_MODIFY;
        }
        if (mChbNotifyAttrib.isChecked()) {
            flags |= MonitorNotifyConstants.NOTIFY_ATTRIB;
        }
        if (mChbNotifyCloseWrite.isChecked()) {
            flags |= MonitorNotifyConstants.NOTIFY_CLOSE_WRITE;
        }
        if (mChbNotifyCloseNoWrite.isChecked()) {
            flags |= MonitorNotifyConstants.NOTIFY_CLOSE_NOWRITE;
        }
        if (mChbNotifyOpen.isChecked()) {
            flags |= MonitorNotifyConstants.NOTIFY_OPEN;
        }
        if (mChbNotifyMovedFrom.isChecked()) {
            flags |= MonitorNotifyConstants.NOTIFY_MOVED_FROM;
        }
        if (mChbNotifyMovedTo.isChecked()) {
            flags |= MonitorNotifyConstants.NOTIFY_MOVED_TO;
        }
        if (mChbNotifyCreate.isChecked()) {
            flags |= MonitorNotifyConstants.NOTIFY_CREATE;
        }
        if (mChbNotifyDelete.isChecked()) {
            flags |= MonitorNotifyConstants.NOTIFY_DELETE;
        }
        if (mChbNotifyMoveSelf.isChecked()) {
            flags |= MonitorNotifyConstants.NOTIFY_MOVE_SELF;
        }
        if (mChbNotifyDeleteSelf.isChecked()) {
            flags |= MonitorNotifyConstants.NOTIFY_DELETE_SELF;
        }
        return flags;
    }
}
