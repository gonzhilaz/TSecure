/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.view.impl;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.documentfile.provider.DocumentFile;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.net.Uri;
import android.provider.MediaStore;

import com.kavsdkexample.core.app.utils.PermissionUtils;
import com.kavsdkexample.core.BuildConfig;
import com.kavsdkexample.core.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Locale;

/**
 * Fragment provides functionality for selecting the directory document
 */
public class DirectoryDocumentChooserFragment extends BaseDialogFragment {
    private static final String TAG = DirectoryDocumentChooserFragment.class.getSimpleName();
    private static final boolean DEBUG = true;
    private static final String EXTRA_INIT_URI = "init_uri";
    private static final String EXTRA_MODE = "mode";
    private static final int REQUEST_CODE_OPEN_DIRECTORY = 1;
    private static final String CHOOSING_DIALOG_ID = "CHOOSING_DIALOG_ID";

    public static final int FILE_MODE = 0;
    public static final int DIRECTORY_MODE = 1;
    public static final int SDCARD_MODE = 2;

    private Uri mUri;
    private TextView mSelectedDir;
    private TextView mHintEmptySubfolders;
    DirectoryChooserAdapter mAdapter;
    private int mMode;
    private String mSelectedVolume;

    public static void show(FragmentActivity activity, String uri, int mode, int requestId, String parentFragmentTag) {
        Bundle args = new Bundle();
        args.putString(EXTRA_INIT_URI, uri);
        args.putInt(EXTRA_MODE, mode);
        BaseDialogFragment.show(activity, DirectoryDocumentChooserFragment.class, args, requestId, parentFragmentTag);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments == null) {
            mMode = DIRECTORY_MODE;
        } else {
            mUri = Uri.parse(arguments.getString(EXTRA_INIT_URI));
            mMode = arguments.getInt(EXTRA_MODE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_directory_chooser_dialog, container, false);
        initControls(root);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mMode == SDCARD_MODE) {
            obtainPermission();
        } else if (mUri == null || !mAdapter.update(mUri)) {
            Uri treeUri = null;
            final Context context = requireContext();
            ContentResolver contentResolver = context.getContentResolver();
            List<UriPermission> urisPermission = contentResolver.getPersistedUriPermissions();
            if (!urisPermission.isEmpty()) {
                for (UriPermission uriPermission : urisPermission) {
                    if (isExternalDeviceSelected(uriPermission.getUri(), context)) {
                        treeUri = uriPermission.getUri();
                        if (treeUri != null && (mUri == null || treeUri.toString().startsWith(mUri.toString()))) {
                            break;
                        }
                        treeUri = null;
                    }
                }
            }

            if (treeUri != null) {
                mAdapter.update(treeUri);
            } else {
                sendActionOpenDocumentTreeIntent(mUri);
            }
        }
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        //noinspection ConstantConditions
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private void obtainPermission() {
        if (mUri == null) {
            return;
        }

        Context context = requireContext();
        final String[] pathSet = mUri.toString().split("/");
        if (pathSet.length <= 1) {
            return;
        }

        String directoryName = pathSet[pathSet.length - 1];
        if (directoryName.isEmpty()) {
            return;
        }

        final Locale local = Locale.getDefault();
        directoryName = directoryName.toLowerCase(local);
        Set<String> volumes = MediaStore.getExternalVolumeNames(context);
        for (String volume : volumes) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "obtainPermission(): directoryName: " + directoryName);
                Log.d(TAG, "obtainPermission(): volume: " + volume);
            }
            if (directoryName.compareTo(volume.toLowerCase(local)) == 0) {
                mSelectedVolume = volume;
                break;
            }
        }

        if (mSelectedVolume == null && volumes.contains(MediaStore.VOLUME_EXTERNAL_PRIMARY)) {
            mSelectedVolume = MediaStore.VOLUME_EXTERNAL_PRIMARY;
        }

        if (!ifVolumeHasPermission(mSelectedVolume, context)) {
            sendActionOpenDocumentTreeIntent(mUri);
        } else {
            dismissAllowingStateLoss();
        }
    }

    private static boolean ifVolumeHasPermission(final String volume, Context context) {
        if (volume == null && context == null) {
            return false;
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "ifVolumeHasPermission(): volume: " + volume);
        }
        ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null) {
            return false;
        }

        List<UriPermission> urisPermission = contentResolver.getPersistedUriPermissions();
        if (!urisPermission.isEmpty()) {
            for (UriPermission uriPermission : urisPermission) {
                final Uri uri = uriPermission.getUri();
                if (uri == null) {
                    continue;
                }

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "ifVolumeHasPermission(): uri: " + uri.toString());
                }
                final String documentId = PermissionUtils.getTreeDocumentId(uri);
                if (documentId == null) {
                    continue;
                }

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "ifVolumeHasPermission(): documentId: " + documentId);
                }
                final String[] set = documentId.split(":");
                if (set.length != 1) {
                    continue;
                }

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "ifVolumeHasPermission(): set[0]: " + set[0]);
                }
                final Locale local = Locale.getDefault();
                if (volume != null && MediaStore.VOLUME_EXTERNAL_PRIMARY.compareTo(volume) == 0 &&
                    set[0].compareTo("primary") == 0 ||
                    set[0].toLowerCase(local).compareTo(volume.toLowerCase(local)) == 0) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "ifVolumeHasPermission(): volume '" + volume + "' has permission");
                    }
                    return true;
                }
            }
        }

        return false;
    }

    private void initControls(View root) {
        ListView listView = root.findViewById(android.R.id.list);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (mAdapter != null) {
                mAdapter.down(position);
            }
        });

        mSelectedDir         = root.findViewById(android.R.id.text1);
        mHintEmptySubfolders = root.findViewById(android.R.id.hint);

        View v = root.findViewById(android.R.id.icon);
        v.setOnClickListener(v1 -> onUpDirectoryClick());
        v = root.findViewById(android.R.id.button1);
        v.setOnClickListener(v12 -> onCancelClick());
        v = root.findViewById(android.R.id.button2);
        v.setEnabled(mMode == DIRECTORY_MODE);
        v.setOnClickListener(v13 -> onOkClick());

        mAdapter = new DirectoryChooserAdapter(this, mMode);
        listView.setAdapter(mAdapter);
    }

    protected void onUpDirectoryClick() {
        if (mAdapter != null) {
            mAdapter.up();
        }
    }

    protected void onOkClick() {
        if (mAdapter == null || mAdapter.getSelectedUri() == null) {
            if (DEBUG) {
                Log.d(TAG, "onOkClick: mAdapter is null");
            }
            return;
        }

        if (DEBUG) {
            Log.d(TAG, "Selected directory: " + mAdapter.getSelectedUri());
        }
        setDialogResult(mAdapter.getSelectedUri());
        dismissAllowingStateLoss();
    }

    protected void onCancelClick() {
        dismissAllowingStateLoss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_DIRECTORY && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null) {
                if (!isExternalDeviceSelected(uri, getActivity()) || mMode == SDCARD_MODE && !isVolumePartOfUri(uri, mSelectedVolume)) {
                    ChoosingDialogFragment choosingDialogFragment = new ChoosingDialogFragment();
                    choosingDialogFragment.setUri(uri);
                    choosingDialogFragment.setParent(this);
                    choosingDialogFragment.setMode(mMode);
                    choosingDialogFragment.setVolume(mSelectedVolume);
                    choosingDialogFragment.show(requireFragmentManager(), CHOOSING_DIALOG_ID);
                } else {
                    requireActivity().getContentResolver().takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    if (mAdapter != null) {
                        mAdapter.update(uri);
                    }

                    if (mMode == SDCARD_MODE) {
                        dismissAllowingStateLoss();
                    }
                }
            }
        }
    }

    private void UriIsChanged(String documentId, int count) {
        mSelectedDir.setText(documentId);
        mHintEmptySubfolders.setVisibility(count > 0 ? View.GONE : View.VISIBLE);
    }

    private void UriIsSelected(Uri uri) {
        setDialogResult(uri.toString());
        dismissAllowingStateLoss();
    }

    private void sendActionOpenDocumentTreeIntent(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        if (uri != null) {
            String treeDocumentId = PermissionUtils.getTreeDocumentId(uri);
            if (treeDocumentId != null) {
                uri = DocumentsContract.buildRootUri(uri.getAuthority(), treeDocumentId);
            }
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
        }
        startActivityForResult(intent, REQUEST_CODE_OPEN_DIRECTORY);
    }

    private void badChosing() {
        dismissAllowingStateLoss();
    }

    /************************************* Inner classes *****************************************/
    private static class DirectoryChooserAdapter extends BaseAdapter {
        private final DirectoryDocumentChooserFragment mFragment;
        private final LayoutInflater mLi;
        private final int mMode;
        private DocumentFile mDocumentFile;
        private List<DocumentFile> mChildren = new ArrayList<>();

        DirectoryChooserAdapter(DirectoryDocumentChooserFragment fragment, int mode) {
            mFragment = fragment;
            mLi = LayoutInflater.from(mFragment.getActivity());
            mMode = mode;
        }

        private void update(DocumentFile documentFile) {
            mChildren.clear();
            mDocumentFile = documentFile;
            for (DocumentFile child : documentFile.listFiles()) {
                if (mMode == FILE_MODE || mMode == DIRECTORY_MODE && child.isDirectory()) {
                    mChildren.add(child);
                }
            }

            mFragment.UriIsChanged(mDocumentFile.getUri().getLastPathSegment(), mChildren.size());
            notifyDataSetChanged();
        }

        public boolean update(Uri uri) {
            if (uri == null) {
                return false;
            }

            DocumentFile documentFile = null;
            try {
                documentFile = DocumentFile.fromTreeUri(mFragment.requireActivity(), uri);
            } catch (java.lang.IllegalArgumentException ignored) {
            }

            String uriString = Uri.decode(uri.toString());
            int pos = uriString.lastIndexOf(':');
            if (pos == -1) {
                return false;
            }

            if (!(uriString = uriString.substring(pos + 1)).isEmpty()) {
                final String[] pathSegment = uriString.split("/");
                for (int i = 0; i < pathSegment.length && documentFile != null; i++) {
                    documentFile = documentFile.findFile(pathSegment[i]);
                }
            }

            if (documentFile != null) {
                update(documentFile);
                return true;
            }

            return false;
        }

        void up() {
            DocumentFile parentDocumentFile;
            if (mFragment == null || mDocumentFile == null || (parentDocumentFile = mDocumentFile.getParentFile()) == null) {
                return;
            }
            update(parentDocumentFile);
        }

        void down(int position) {
            DocumentFile documentFile = getItem(position);
            if (documentFile == null) {
                return;
            }

            if (documentFile.isDirectory()) {
                update(documentFile);
            } else {
                mFragment.UriIsSelected(documentFile.getUri());
            }
        }

        private String getSelectedUri() {
            if (mDocumentFile == null) {
                return null;
            }
            return mDocumentFile.getUri().toString();
        }

        @Override
        public int getCount() {
            return mChildren.size();
        }

        @Override
        public DocumentFile getItem(int position) {
            return mChildren.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            ViewHolder holder;
            if (v == null) {
                v = mLi.inflate(android.R.layout.simple_list_item_1, parent, false);
                holder = new ViewHolder(v);
                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }

            DocumentFile element = getItem(position);
            holder.mTextView.setText(element.getName());
            holder.mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                element.isDirectory() ? holder.mInitTextSize : holder.mInitTextSize *3/4);
            return v;
        }

        private static class ViewHolder {
            TextView mTextView;
            final float mInitTextSize;

            ViewHolder(View root) {
                mTextView = root.findViewById(android.R.id.text1);
                mInitTextSize = mTextView.getTextSize();
            }
        }
    }

    public static class ChoosingDialogFragment extends DialogFragment {
        private Uri mUri;
        private DirectoryDocumentChooserFragment mParent;
        private String mVolume;
        private int mMode;

        public void setUri(Uri uri) {
            mUri = uri;
        }

        public void setParent(DirectoryDocumentChooserFragment parent) {
            mParent = parent;
        }

        public void setMode(int mode) {
            mMode = mode;
        }

        public void setVolume(final String volume) {
            mVolume = volume;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            setRetainInstance(true);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.str_directorydocumentchooserfragment_failed_to_choose_text))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> mParent.sendActionOpenDocumentTreeIntent(null))
                .setNegativeButton(android.R.string.no, (dialog, which) -> mParent.badChosing());

            if (mMode == SDCARD_MODE) {
                alertDialogBuilder.setMessage(getString(R.string.str_directorydocumentchooserfragment_chosen_wrong_volume, Uri.decode(mUri.toString()), mVolume));
            } else {
                alertDialogBuilder.setMessage(getString(R.string.str_directorydocumentchooserfragment_chosen_root_point, Uri.decode(mUri.toString())));
            }

            return alertDialogBuilder.create();
        }
    }

    @SuppressLint("NewApi")
    public static boolean isVolumePartOfUri(final Uri uri, final String volume) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || uri == null) {
            return false;
        }

        if (DEBUG) {
            Log.d(TAG, "isVolumePartOfUri(): uri: " + uri.toString() + ", volume: " + volume);
        }

        final String documentId = PermissionUtils.getTreeDocumentId(uri);
        if (documentId == null) {
            return false;
        }

        final String[] pathSet = documentId.split(":");
        if (pathSet.length != 1) {
            return false;
        }

        if (DEBUG) {
            Log.d(TAG, "ifVolumePartOfUri(): pathSet[0]: " + pathSet[0]);
        }

        return ((volume.compareTo(MediaStore.VOLUME_EXTERNAL_PRIMARY) == 0) && (pathSet[0].compareTo("primary") == 0)) ||
                (pathSet[0].toLowerCase(Locale.getDefault()).compareTo(volume.toLowerCase(Locale.getDefault())) == 0);
    }

    @SuppressLint("NewApi")
    public static boolean isExternalDeviceSelected(final Uri uri, Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || uri == null) {
            return false;
        }

        final String documentId = PermissionUtils.getTreeDocumentId(uri);
        if (documentId == null) {
            return false;
        }

        final String[] pathSet = documentId.split(":");
        if (pathSet.length != 1) {
            return false;
        }

        pathSet[0] = pathSet[0].toLowerCase(Locale.getDefault());
        Set<String> volumes = MediaStore.getExternalVolumeNames(context);
        for (String volume : volumes) {
            if ((pathSet[0].compareTo(volume.toLowerCase(Locale.getDefault())) == 0) ||
                    ((MediaStore.VOLUME_EXTERNAL_PRIMARY.compareTo(volume) == 0) && (pathSet[0].compareTo("primary") == 0))) {
                return true;
            }
        }

        return false;
    }
}