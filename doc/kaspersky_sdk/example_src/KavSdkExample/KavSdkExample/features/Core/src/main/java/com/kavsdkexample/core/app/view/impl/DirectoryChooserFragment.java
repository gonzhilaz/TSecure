/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.view.impl;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.kavsdk.utils.Utils;
import com.kavsdkexample.core.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Fragment provides functionality for selecting the directory
 */
public class DirectoryChooserFragment extends BaseDialogFragment {
    private static final String TAG = DirectoryChooserFragment.class.getSimpleName();
    private static final boolean DEBUG = true;
    private static final String EXTRA_INIT_DIR = "init_dir";
    private static final String EXTRA_MODE = "mode";

    public static final int FILE_MODE = 0;
    public static final int DIRECTORY_MODE = 1;

    private File mCurrentDir;
    private TextView mSelectedDir;
    private ListView mListView;
    private TextView mHintEmptySubfolders;
    private int mMode;

    public static void show(FragmentActivity activity, File initDir, int mode, int requestId, String parentFragmentTag) {
        Bundle args = new Bundle();
        args.putString(EXTRA_INIT_DIR, initDir.getAbsolutePath());
        args.putInt(EXTRA_MODE, mode);
        BaseDialogFragment.show(activity, DirectoryChooserFragment.class, args, requestId, parentFragmentTag);
    }

    private static File extractInitDir(Bundle args) {
        String path;
        if (args == null) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            path = args.getString(EXTRA_INIT_DIR);
            if (path == null) {
                throw new IllegalStateException("Expected not null path");
            }
        }
        return new File(path);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentDir = extractInitDir(getArguments());
        mMode = getArguments() == null ? DIRECTORY_MODE : getArguments().getInt(EXTRA_MODE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_directory_chooser_dialog, container, false);
        initControls(root);
        return root;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        //noinspection ConstantConditions
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private void initControls(View root) {

        mListView = root.findViewById(android.R.id.list);
        mListView.setOnItemClickListener((parent, view, position, id) -> {
            DirectoryChooserAdapter adapter = (DirectoryChooserAdapter) mListView.getAdapter();
            File file = adapter.getItem(position);
            if (mMode == DIRECTORY_MODE || mMode == FILE_MODE && file.isDirectory()) {
                refreshAdapter(file);
            } else {
                if (DEBUG) {
                    Log.d(TAG, "Selected directory: " + mCurrentDir.getAbsolutePath());
                }
                setDialogResult(file);
                dismissAllowingStateLoss();
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

        refreshAdapter(mCurrentDir);
    }

    protected void onUpDirectoryClick() {
        DirectoryChooserAdapter adapter = (DirectoryChooserAdapter) mListView.getAdapter();
        File fDir = adapter.getSelectedDir();
        if (fDir.getParentFile() != null) {
            refreshAdapter(fDir.getParentFile());
        }
    }

    protected void onOkClick() {
        if (DEBUG) {
            Log.d(TAG, "Selected directory: " + mCurrentDir.getAbsolutePath());
        }
        setDialogResult(mCurrentDir);
        dismissAllowingStateLoss();
    }

    protected void onCancelClick() {
        dismissAllowingStateLoss();
    }

    private void refreshAdapter(File dir) {
        DirectoryChooserAdapter adapter = new DirectoryChooserAdapter(getActivity(), dir, mMode);
        mListView.setAdapter(adapter);
        mSelectedDir.setText(dir.getAbsolutePath());
        mHintEmptySubfolders.setVisibility(adapter.getCount() > 0 ? View.GONE : View.VISIBLE);
        mCurrentDir = dir;
    }

    /************************************* Inner classes *****************************************/
    private static class DirectoryChooserAdapter extends BaseAdapter {
        private static final String TAG = DirectoryChooserFragment.DirectoryChooserAdapter.class
                .getSimpleName();

        private final LayoutInflater mLi;
        private final Handler mHandler;
        private final int mMode;

        private List<File> mFiles;
        private File mSelectedDir;
        private FileObserver mFileObserver;

        DirectoryChooserAdapter(Context context, File parent, int mode) {
            mLi = LayoutInflater.from(context);
            mHandler = new Handler(context.getMainLooper());
            mMode = mode;
            changeDirectory(parent);
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                if (mFileObserver != null) {
                    mFileObserver.stopWatching();
                }
            } finally {
                super.finalize();
            }
        }

        void changeDirectory(File parent) {
            mFiles = new ArrayList<>();
            File[] contents = Utils.getItemsForPath(parent);
            if (contents != null) {
                for (File f : contents) {
                    if (mMode == FILE_MODE || mMode == DIRECTORY_MODE && f.isDirectory()) {
                        mFiles.add(f);
                    }
                }
            }

            Collections.sort(mFiles, (lhs, rhs) -> !lhs.isDirectory() && rhs.isDirectory() ? 1 :
                (lhs.isDirectory() && !rhs.isDirectory() ? -1 :
                        lhs.getName().toLowerCase(Locale.getDefault()).
                                compareTo(rhs.getName().toLowerCase(Locale.getDefault()))));

            mSelectedDir = parent;

            //stop previous observer
            if (mFileObserver != null) {
                mFileObserver.stopWatching();
            }

            mFileObserver = createFileObserver(parent.getAbsolutePath());
            mFileObserver.startWatching();
        }

        File getSelectedDir() {
            return mSelectedDir;
        }

        /**
         * Sets up a FileObserver to watch the current directory.
         */
        private FileObserver createFileObserver(String path) {
            return new FileObserver(path, FileObserver.CREATE | FileObserver.DELETE
                    | FileObserver.MOVED_FROM | FileObserver.MOVED_TO) {

                @Override
                public void onEvent(int event, String path) {
                    if (!TextUtils.isEmpty(path)) {
                        if (DEBUG) {
                            Log.d(TAG, "FileObserver received event " + event + " , path = "+ path);
                        }
                        mHandler.post(() -> {
                            changeDirectory(mSelectedDir);
                            notifyDataSetChanged();
                        });
                    }
                }
            };
        }

        @Override
        public int getCount() {
            return mFiles.size();
        }

        @Override
        public File getItem(int position) {
            return mFiles.get(position);
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

            File file = getItem(position);
            holder.mTextView.setText(file.getName());
            holder.mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    file.isDirectory() ? holder.mInitTextSize : holder.mInitTextSize *3/4);
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
}
