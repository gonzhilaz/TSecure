/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.appcontrol.view.impl;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.widget.SwitchCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kavsdkexample.appcontrol.R;
import com.kavsdkexample.appcontrol.R2;
import com.kavsdkexample.appcontrol.presenter.AppControlPresenter;
import com.kavsdkexample.appcontrol.view.AppControlView;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.core.ui.BaseActivity;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.AndroidInjection;

public class AppControlActivity extends BaseActivity<AppControlView, BaseViewState, AppControlPresenter>
        implements AppControlView, View.OnClickListener, OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener {
    private static final String DELETE_DIALOG_ID = "DELETE_DIALOG_TAG";
    private static final String UNABLE_TO_DELETE_DIALOG_ID = "UNABLE_TO_DELETE_DIALOG_ID";
    private ItemsAdapter mItemsAdapter;
    private int mSelectedIndex;
    private boolean mBlockList;

    @Inject
    AppControlPresenter mPresenter;

    @BindView(R2.id.enableAppcontrol) SwitchCompat mSwitchCompatEnableAppcontrol;
    @BindView(R2.id.useWindowManagerForBlocking) SwitchCompat mSwitchCompatUseWindowManagerForBlocking;
    @BindView(R2.id.radioGroupControlMode) RadioGroup mRadioGroupRadioGroupControlMode;
    @BindView(R2.id.appControlItemsList) ListView mListViewAppcontrolItemsList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_control_activity);
        AndroidInjection.inject(this);
        ButterKnife.bind(this);
        mSwitchCompatEnableAppcontrol.setOnCheckedChangeListener(this);
        mSwitchCompatEnableAppcontrol.setChecked(mPresenter.isAppcontrolEnabled());
        mSwitchCompatUseWindowManagerForBlocking.setOnCheckedChangeListener(this);
        mSwitchCompatUseWindowManagerForBlocking.setChecked(mPresenter.isWindowManagerForBlockingUsed());
        mRadioGroupRadioGroupControlMode.setOnCheckedChangeListener(this);

        mItemsAdapter = new ItemsAdapter(this, mPresenter);
        mListViewAppcontrolItemsList.setAdapter(mItemsAdapter);
        mListViewAppcontrolItemsList.setOnItemClickListener((parent, view, position, id) -> {
            if (!view.isEnabled()) {
                return;
            }

            boolean hitItem = false;
            if (position > 0 && position < (getPresenter().getBlockListItemsCount() + 1)) {
                mBlockList = true;
                mSelectedIndex = position - 1;
                hitItem = true;
            }

            if (position > (getPresenter().getBlockListItemsCount() + 1)) {
                mBlockList = false;
                mSelectedIndex = position - getPresenter().getBlockListItemsCount() - 2;
                hitItem = true;
            }

            if (hitItem) {
                if (mBlockList) {
                    mPresenter.requestRemovalFromBlockList(mSelectedIndex);
                } else {
                    mPresenter.requestRemovalFromAllowList(mSelectedIndex);
                }
            }
        });
    }

    @Override
    protected AppControlPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected BaseViewState getViewState() {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    @OnClick({ R2.id.openAccessibilitySettings,
               R2.id.addItem })
    public void onClick(View v) {
        final int id            = v.getId();
        if (id == R.id.openAccessibilitySettings) {
            mPresenter.openSettings();
        } else if (id == R.id.addItem) {
            AddAppItemDialogFragment dialog = new AddAppItemDialogFragment();
            dialog.setActivity(this);
            dialog.show(getSupportFragmentManager(), AddAppItemDialogFragment.class.getSimpleName());
        } else {
            throw new IllegalStateException("Unsupported view id: " + id);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        final int id = buttonView.getId();
        if (id == R.id.enableAppcontrol) {
            mPresenter.enableAppControl(isChecked);
        } else if (id == R.id.useWindowManagerForBlocking) {
            mPresenter.useWindowManagerForBlocking(isChecked);
        } else {
            throw new IllegalStateException("Unsupported view id: " + id);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group == mRadioGroupRadioGroupControlMode) {
            if (checkedId == R.id.useBothLists) {
                mPresenter.setBothListsMode();
            } else if (checkedId == R.id.useBlockList) {
                mPresenter.setBlockListMode();
            } else if (checkedId == R.id.useAllowList) {
                mPresenter.setAllowListMode();
            }
            mPresenter.saveChanges();
        }
    }

    @Override
    public void onPackageNameNotAllowedInBlockList(@Nullable final String packageName) {
        Toast
                .makeText(this, R.string.str_appcontrol_add_self_toast_message, Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void onPackageNameNotAllowedInAllowList(@Nullable final String packageName) {
        // currently impossible
    }

    @Override
    public void requestRemovalConfirmation(@NonNull final AppControlPresenter.RemovalConfirmationListener listener) {
        final DeleteDialogFragment deleteDialogFragment = new DeleteDialogFragment();
        deleteDialogFragment.init(mPresenter, mItemsAdapter, listener);
        deleteDialogFragment.show(getSupportFragmentManager(), DELETE_DIALOG_ID);
    }

    @Override
    public void onIndexNotAllowedToRemoveFromBlockList(final int index) {
        // currently impossible
    }

    @Override
    public void onIndexNotAllowedToRemoveFromAllowList(final int index) {
        final UnableToDeleteDialogFragment fragment = new UnableToDeleteDialogFragment();
        fragment.show(getSupportFragmentManager(), UNABLE_TO_DELETE_DIALOG_ID);
    }

    @NonNull
    ItemsAdapter getItemsAdapter() {
        return mItemsAdapter;
    }

    public static class AddAppItemDialogFragment extends DialogFragment {

        private static final int INDEX_BLOCK_LIST = 0;
        private static final int INDEX_ALLOW_LIST = 1;

        private AppControlActivity mAppControlActivity;

        public void setActivity(AppControlActivity appControlActivity) {
            mAppControlActivity = appControlActivity;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();
            final Dialog dialog = new Dialog(context);

            dialog.setContentView(R.layout.appcontrol_add_item_dialog);
            dialog.setTitle(R.string.str_appcontrol_add_item_button);
            setRetainInstance(true);

            ArrayAdapter<CharSequence> adapterListTypeSpinner = ArrayAdapter.createFromResource(
                context, R.array.appcontrol_lists, android.R.layout.simple_spinner_item);
            adapterListTypeSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            final Spinner listTypeSpinner = dialog.findViewById(R.id.appcontrol_add_dialog_list_type_spinner);
            listTypeSpinner.setAdapter(adapterListTypeSpinner);

            final Spinner categorySpinner = dialog.findViewById(R.id.appcontrol_add_dialog_item_category_spinner);
            if (mAppControlActivity != null) {
                List<String> categories = mAppControlActivity.getPresenter().getCategories();
                categories.add(0, "any");
                ArrayAdapter<String> adapterCategorySpinner = new ArrayAdapter<>(
                        context, android.R.layout.simple_spinner_item, categories);
                adapterCategorySpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySpinner.setAdapter(adapterCategorySpinner);
            }

            final EditText packageNameText = dialog.findViewById(R.id.appcontrol_add_dialog_package_name_text);

            Button addButton = dialog.findViewById(R.id.appcontrol_add_dialog_add_button);
            addButton.setOnClickListener(v -> {
                String packageName = packageNameText.getText().toString();
                if (packageName.isEmpty()) {
                    packageName = null;
                }
                String category = null;
                int selectedCategoryIndex = categorySpinner.getSelectedItemPosition();
                if (selectedCategoryIndex != 0) { //0 is "any"
                    category = categorySpinner.getAdapter().getItem(selectedCategoryIndex).toString();
                }
                if (packageName == null && category == null) {
                    Toast.makeText(getActivity(), "Please specify package or category", Toast.LENGTH_SHORT).show();
                    return;
                }
                switch (listTypeSpinner.getSelectedItemPosition()) {
                    case INDEX_BLOCK_LIST:
                        mAppControlActivity.getPresenter().addItemToBlockList(packageName, category);
                        break;
                    case INDEX_ALLOW_LIST:
                        mAppControlActivity.getPresenter().addItemToAllowList(packageName, category);
                        break;
                    default:
                        throw new RuntimeException("Wrong index");
                }
                mAppControlActivity.getItemsAdapter().notifyDataSetChanged();
                mAppControlActivity.getPresenter().saveChanges();
                dismiss();
            });

            Button cancelButton = dialog.findViewById(R.id.appcontrol_add_dialog_cancel_button);
            cancelButton.setOnClickListener(v -> dialog.cancel());

            return dialog;
        }

        @Override
        public void onDestroyView() {

            if (getDialog() != null && getRetainInstance()) {
                getDialog().setOnDismissListener(null);
            }
            super.onDestroyView();
        }
    }

    private static class ItemsAdapter extends BaseAdapter {

        private static final int TYPE_ITEM = 0;
        private static final int TYPE_SEPARATOR = 1;
        private static final int TYPE_COUNT = TYPE_SEPARATOR + 1;

        private final LayoutInflater mInflater;
        private final Resources mResources;
        private final AppControlPresenter mPresenter;

        ItemsAdapter(final Context context, final AppControlPresenter presenter) {
            mInflater = LayoutInflater.from(context);
            mResources = context.getResources();
            mPresenter = presenter;
        }

        @Override
        public int getViewTypeCount() {
            return TYPE_COUNT;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0 || position == mPresenter.getBlockListItemsCount() + 1) {
                return TYPE_SEPARATOR;
            }
            return TYPE_ITEM;
        }

        @Override
        public int getCount() {
            return mPresenter.getBlockListItemsCount() + mPresenter.getAllowListItemsCount() + 2;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            int type = getItemViewType(position);
            if (convertView == null) {
                holder = new ViewHolder();
                switch (type) {
                    case TYPE_ITEM:
                        convertView = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
                        holder.mTextView = convertView.findViewById(android.R.id.text1);
                        holder.mTextView.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX,
                                mResources.getDimensionPixelSize(R.dimen.text_small)
                        );
                        break;
                    case TYPE_SEPARATOR:
                        convertView = mInflater.inflate(R.layout.appcontrol_items_separator, parent, false);
                        holder.mTextView = convertView.findViewById(R.id.appcontrol_separator_text);
                        break;
                    default:
                        throw new RuntimeException("Wrong item type");
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            if (position == 0) {
                //Block list header
                holder.mTextView.setText(mResources.getString(R.string.str_appcontrol_blocklist_header));
            } else if (position < (mPresenter.getBlockListItemsCount() + 1)) {
                //Block list itself
                int index = position - 1;
                holder.mTextView.setText(
                        getItemTitle(
                                mPresenter.getBlockListPackage(index),
                                mPresenter.getBlockListCategory(index)
                        )
                );
            } else if (position == (mPresenter.getBlockListItemsCount() + 1)) {
                //Allow list header
                holder.mTextView.setText(mResources.getString(R.string.str_appcontrol_allowlist_header));
            } else {
                //Block list itself
                final int index = position - mPresenter.getBlockListItemsCount() - 2;
                holder.mTextView.setText(
                        getItemTitle(
                                mPresenter.getAllowListPackage(index),
                                mPresenter.getAllowListCategory(index)
                        )
                );
            }

            convertView.setEnabled(parent.isEnabled());
            return convertView;
        }

        private String getItemTitle(String itemPackage, String category) {
            return String.format(
                    "%s, %s",
                    getTitle(itemPackage, R.string.str_appcontrol_any_package),
                    getTitle(category, R.string.str_appcontrol_any_category)
            );
        }

        private String getTitle(@Nullable final String value, @StringRes final int defaultValueRes) {
            return (value != null) ? value : mResources.getString(defaultValueRes);
        }
    }

    public static class AppControlDialogFragment extends DialogFragment {
        @Override
        public void onDestroyView() {
            if ((getDialog() != null) && getRetainInstance()) {
                getDialog().setOnDismissListener(null);
            }
            super.onDestroyView();
        }
    }

    public static class DeleteDialogFragment extends AppControlDialogFragment {
        private AppControlPresenter mPresenter;
        private ItemsAdapter mItemsAdapter;
        private AppControlPresenter.RemovalConfirmationListener mRemovalConfirmationListener;

        public void init(@NonNull final AppControlPresenter presenter,
                         @NonNull final ItemsAdapter itemsAdapter,
                         @NonNull final AppControlPresenter.RemovalConfirmationListener listener) {
            mRemovalConfirmationListener = listener;
            mPresenter = presenter;
            mItemsAdapter = itemsAdapter;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            setRetainInstance(true);
            return new AlertDialog
                    .Builder(getActivity())
                    .setTitle(R.string.str_appcontrol_delete_dialog_title)
                    .setMessage(R.string.str_appcontrol_delete_dialog_confirm)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        mRemovalConfirmationListener.onRemoveConfirmed();
                        mItemsAdapter.notifyDataSetChanged();
                        mPresenter.saveChanges();
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .create();
        }
    }

    public static class UnableToDeleteDialogFragment extends AppControlDialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
            return new AlertDialog
                    .Builder(getActivity())
                    .setTitle(R.string.str_appcontrol_delete_self_dialog_title)
                    .setMessage(R.string.str_appcontrol_delete_self_dialog_message)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
        }
    }

    private static class ViewHolder {
        TextView mTextView;
    }
}