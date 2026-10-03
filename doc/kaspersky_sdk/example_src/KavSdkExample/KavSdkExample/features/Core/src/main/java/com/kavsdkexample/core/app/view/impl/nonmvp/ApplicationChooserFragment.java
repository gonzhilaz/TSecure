/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.view.impl.nonmvp;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.kavsdkexample.core.R;
import com.kavsdkexample.core.app.utils.AndroidUtils;
import com.kavsdkexample.core.app.view.impl.BaseDialogFragment;

import java.util.List;

public class ApplicationChooserFragment extends BaseDialogFragment {
    public static final int APPLICATION_MODE = 0;

    private static final String EXCLUDE_SYSTEM_EXTRA = "exclude_system";
    private static final String EXCLUDE_SELF_EXTRA   = "exclude_self";

    private ListView mListView;
    private TextView mHintEmptySubFolders;
    private boolean mExcludeSystemApps;
    private boolean mExcludeSelfApp;

    public static void show(FragmentActivity activity, boolean excludeSystem, boolean excludeSelf, String parentFragmentTag) {
        Bundle args = new Bundle();
        args.putBoolean(EXCLUDE_SYSTEM_EXTRA, excludeSystem);
        args.putBoolean(EXCLUDE_SELF_EXTRA, excludeSelf);
        show(activity, ApplicationChooserFragment.class, args, APPLICATION_MODE, parentFragmentTag);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_application_chooser_dialog, container, false);
        initControls(root);
        return root;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        //noinspection ConstantConditions
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        Bundle arguments = getArguments();
        if (arguments == null) {
            throw new IllegalStateException("Expected non null arguments for dialog");
        }

        mExcludeSystemApps = arguments.getBoolean(EXCLUDE_SYSTEM_EXTRA);
        mExcludeSelfApp = arguments.getBoolean(EXCLUDE_SELF_EXTRA);

        return dialog;
    }

    private void initControls(View root) {
        mListView            = root.findViewById(android.R.id.list);
        mHintEmptySubFolders = root.findViewById(android.R.id.hint);

        mListView.setOnItemClickListener((parent, view, position, id) -> {
            ApplicationInfo app = (ApplicationInfo)mListView.getAdapter().getItem(position);
            setDialogResult(app.packageName);
            dismissAllowingStateLoss();
        });

        refreshAdapter();

        View v = root.findViewById(android.R.id.button1);
        v.setOnClickListener(v12 -> onCancelClick());
        v = root.findViewById(android.R.id.button2);
        v.setOnClickListener(v1 -> onOkClick());
    }

    protected void onOkClick() {
        ApplicationInfo app = (ApplicationInfo)mListView.getSelectedItem();
        if (app != null) {
            setDialogResult(app.packageName);
        }
        dismissAllowingStateLoss();
    }

    protected void onCancelClick() {
        dismissAllowingStateLoss();
    }

    private void refreshAdapter() {
        // TODO: Application list should be received in worker thread
        List<ApplicationInfo> apps = AndroidUtils.getApplications(requireContext(), mExcludeSystemApps, mExcludeSelfApp);
        ApplicationChooserAdapter adapter = new ApplicationChooserAdapter(getActivity(), apps);
        mListView.setAdapter(adapter);
        mHintEmptySubFolders.setVisibility(adapter.getCount() > 0 ? View.GONE : View.VISIBLE);
    }

    private static class ApplicationChooserAdapter extends BaseAdapter {

        private final Context mContext;
        private final List<ApplicationInfo> mApps;

        ApplicationChooserAdapter(Context context, List<ApplicationInfo> apps) {
            mContext = context;
            mApps = apps;
        }

        @Override
        public int getCount() {
            return mApps.size();
        }

        @Override
        public Object getItem(int position) {
            return mApps.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ApplicationInfo app = mApps.get(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_2, parent, false);
            }

            PackageManager pm = mContext.getPackageManager();
            CharSequence name = pm.getApplicationLabel(app);

            ((TextView) convertView.findViewById(android.R.id.text1)).setText(name);
            ((TextView) convertView.findViewById(android.R.id.text2)).setText(app.packageName);

            return convertView;
        }
    }
}
