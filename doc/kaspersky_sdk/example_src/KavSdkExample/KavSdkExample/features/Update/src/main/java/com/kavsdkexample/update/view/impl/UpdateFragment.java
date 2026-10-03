/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.update.view.impl;

import android.os.Bundle;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.text.TextWatcher;
import android.text.Editable;

import com.kavsdkexample.update.model.UpdateModelComponentMode;
import com.kavsdkexample.update.model.UpdateModelUpdateServerMode;
import com.kavsdkexample.update.model.UpdateResults;
import com.kavsdkexample.update.view.UpdateViewState;
import com.kavsdkexample.update.view.UpdateView;
import com.kavsdkexample.update.presenter.UpdatePresenter;
import com.kavsdkexample.core.ui.BaseFragment;
import com.kavsdkexample.update.R;
import com.kavsdkexample.update.R2;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;

public class UpdateFragment extends BaseFragment<UpdateView, UpdateViewState, UpdatePresenter>
    implements UpdateView, OnClickListener, RadioGroup.OnCheckedChangeListener, TextWatcher {

    private static final String DEFAULT_URL = "http://dnl-test.kaspersky-labs.com/test/mob/";
    private static final String UPDATE_SERVER = "update_server";
    private static final String UPDATE_MODEL_COMPONENT = "update_model_component";
    private static final String UPDATE_MODEL_UPDATE_SERVER = "update_model_update_server";
    private static final String UPDATE_UPDATE_DETAILS = "update_update_details";
    private static final String UPDATE_START_BUTTON_STATE = "update_start_button_state";

    @BindView(R2.id.RadioGroupUpdateComponent)
    RadioGroup mUpdateComponentRadioGroup;

    @BindView(R2.id.RadioGroupUpdateServer)
    RadioGroup mUpdateServerRadioGroup;

    @BindView(R2.id.updateServer)
    EditText mServerEditText;

    @BindView(R2.id.startUpdate)
    Button mStartUpdate;

    @BindView(R2.id.updateDetails)
    TextView mDetailsTextView;

    @BindView(R2.id.defaultServer)
    RadioButton mDefaultServerRadioButton;

    @Inject
    UpdatePresenter mPresenter;

    private Unbinder mUnbinder;

    private UpdateViewState mViewState;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.update_main_fragment, container, false);

        AndroidSupportInjection.inject(this);
        mUnbinder = ButterKnife.bind(this, view);

        mStartUpdate.setOnClickListener(this);
        mUpdateComponentRadioGroup.setOnCheckedChangeListener(this);
        mUpdateServerRadioGroup.setOnCheckedChangeListener(this);
        mServerEditText.addTextChangedListener(this);
        mDefaultServerRadioButton.setText(DEFAULT_URL);

        mViewState = (savedInstanceState == null) ? null : restoreFromBundle(savedInstanceState);

        return view;
    }

    @Override
    @CallSuper
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @NonNull
    @Override
    protected UpdatePresenter getPresenter() {
        return mPresenter;
    }

    @Nullable
    @Override
    protected UpdateViewState getViewState() {
        return mViewState;
    }

    @Override
    public void executionDetails(UpdateResults result) {
        if (UpdateResults.TaskStarted == result) {
            mPresenter.appendUpdateDetails(getString(R.string.str_updates_update_started) + "\n");
        } else if (UpdateResults.BasesDownloaded == result) {
            mPresenter.appendUpdateDetails(getString(R.string.str_updates_bases_downloaded) + "\n");
        } else if (UpdateResults.BasesApplied == result) {
            mPresenter.appendUpdateDetails(getString(R.string.str_updates_bases_applied) + "\n");
        } else if (UpdateResults.UpdateSuccess == result) {
            mPresenter.appendUpdateDetails(getString(R.string.str_updates_update_finished_succesfully) + "\n");
        } else if (UpdateResults.UpdateNoNewBases == result) {
            mPresenter.appendUpdateDetails(getString(R.string.str_updates_update_finished_no_new_bases) + "\n");
        } else if (UpdateResults.UpdateFailedNoConnection == result) {
            mPresenter.appendUpdateDetails(getString(R.string.str_updates_update_finished_failed_no_connection) + "\n");
        } else if (UpdateResults.UpdateFailedNoDiskSpace == result) {
            mPresenter.appendUpdateDetails(getString(R.string.str_updates_update_finished_failed_no_disk_space) + "\n");
        } else if (UpdateResults.UpdateFailed == result) {
            mPresenter.appendUpdateDetails(getString(R.string.str_updates_update_finished_failed) + "\n");
        } else if (UpdateResults.UpdateCanceled == result) {
            mPresenter.appendUpdateDetails(getString(R.string.str_updates_update_finished_canceled) + "\n");
        } else if (UpdateResults.UpdateCanceledDateIncorrect == result) {
            mPresenter.appendUpdateDetails(getString(R.string.str_updates_update_finished_canceled_date_incorrect) + "\n");
        } else if (UpdateResults.UpdateBasesCorrupted == result) {
            mPresenter.appendUpdateDetails(getString(R.string.str_updates_update_finished_bases_corrupted) + "\n");
        } else if (UpdateResults.WrongComponent == result) {
            mPresenter.appendUpdateDetails(getString(R.string.str_wrong_component) + "\n");
        } else if (UpdateResults.ErrorLicenseExpired == result) {
            mPresenter.appendUpdateDetails(getString(R.string.str_error_license_expired) + "\n");
        } else if (UpdateResults.FailedToCreateUpdater == result) {
            mPresenter.appendUpdateDetails(getString(R.string.str_failed_to_create_updater) + "\n");
        }
    }

    @Override
    public void executionDetails(UpdateResults result, int code) {
        if (UpdateResults.TaskFinished == result) {
            mPresenter.appendUpdateDetails(String.format(getString(R.string.str_updates_update_finished_with_result_code), code) + "\n");
        }
    }

    @Override
    public void executionDetails(UpdateResults result, final String details) {
        if (UpdateResults.ServerChanged == result) {
            mPresenter.appendUpdateDetails(String.format(getString(R.string.str_updates_server_changed), details) + "\n");
        } else if (UpdateResults.ServerSelected == result) {
            mPresenter.appendUpdateDetails(String.format(getString(R.string.str_updates_server_selected), details) + "\n");
        } else if (UpdateResults.WaitingForAnotherUpdateFinished == result) {
            mPresenter.appendUpdateDetails(String.format(getString(R.string.str_updates_waiting_for_another_update_finished), details) + "\n");
        } else if (UpdateResults.MalformedURLException == result) {
            mPresenter.appendUpdateDetails(String.format(getString(R.string.str_malformed_url_exception), details) + "\n");
        }
    }

    @Override
    public void updateIsRunning() {
        mPresenter.clearUpdateDetails();

        final int componentRadioButtonID = mUpdateComponentRadioGroup.getCheckedRadioButtonId();
        View componentRadioButton = mUpdateComponentRadioGroup.findViewById(componentRadioButtonID);
        final int selectedComponentRadioButtonID = mUpdateComponentRadioGroup.indexOfChild(componentRadioButton);
        RadioButton selectedComponentRadioButton = (RadioButton) mUpdateComponentRadioGroup.getChildAt(selectedComponentRadioButtonID);
        if (selectedComponentRadioButton != null) {
            final String component = selectedComponentRadioButton.getText().toString();
            mPresenter.appendUpdateDetails(String.format(getString(R.string.str_updates_selected_update_component), component, selectedComponentRadioButtonID) + "\n");
        }

        enableStartUpdateButton(false);
    }

    @Override
    public void updateIsCompleted() {
        enableStartUpdateButton(true);
    }

    @Override
    public void enableStartUpdateButton(boolean enable) {
        mStartUpdate.setEnabled(enable);
        if (enable) {
            mStartUpdate.setText(R.string.str_updates_start_update);
        } else {
            mStartUpdate.setText(R.string.str_updates_cancel_update);
        }
    }

    @Override
    public void setUpdateModelComponent(@NonNull UpdateModelComponentMode component) {
        silentRadioCheck(mUpdateComponentRadioGroup, UpdateFragmentHelper.getComponentId(component));
    }

    @Override
    public void setUpdateServer(@NonNull String updateServer) {
        mServerEditText.setText(updateServer);
    }


    @Override
    public void setUpdateModelUpdateServer(@NonNull UpdateModelUpdateServerMode updateServer) {
        if (updateServer == UpdateModelUpdateServerMode.Default) {
            silentRadioCheck(mUpdateServerRadioGroup, R.id.defaultServer);
        } else if (updateServer == UpdateModelUpdateServerMode.Random) {
            silentRadioCheck(mUpdateServerRadioGroup, R.id.randomServer);
        } else if (updateServer == UpdateModelUpdateServerMode.Specific) {
            silentRadioCheck(mUpdateServerRadioGroup, R.id.specifiedServer);
        }
        mServerEditText.setEnabled(updateServer == UpdateModelUpdateServerMode.Specific);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group == mUpdateComponentRadioGroup) {
            mPresenter.setUpdateModelComponent(UpdateFragmentHelper.getModelComponentMode(checkedId));
        } else if (group == mUpdateServerRadioGroup) {
            mPresenter.setUpdateServer("");
            UpdateModelUpdateServerMode updateServer;
            if (checkedId == R.id.randomServer) {
                updateServer = UpdateModelUpdateServerMode.Random;
            } else if (checkedId == R.id.defaultServer) {
                mPresenter.setUpdateServer(DEFAULT_URL);
                updateServer = UpdateModelUpdateServerMode.Default;
            } else if (checkedId == R.id.specifiedServer) {
                updateServer = UpdateModelUpdateServerMode.Specific;
            } else {
                updateServer = UpdateModelUpdateServerMode.Random;
            }
            mPresenter.setUpdateModelUpdateServer(updateServer);
            mServerEditText.setEnabled(updateServer == UpdateModelUpdateServerMode.Specific);
        }
    }

    @Override
    @OnClick({ R2.id.startUpdate })
    public void onClick(View v) {
        if (v.getId() == R.id.startUpdate) {
            mPresenter.updateRequest();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        mPresenter.setUpdateServer(s.toString());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void clearUpdateDetails() {
        mDetailsTextView.setText("");
    }

    @Override
    public void appendUpdateDetails(@NonNull String details) {
        mDetailsTextView.append(details);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outstate) {
        super.onSaveInstanceState(outstate);
        saveToBundle(outstate,
            new UpdateViewStateImpl(
                mPresenter.getUpdateServer(),
                mPresenter.getUpdateModelComponent().getId(),
                mPresenter.getUpdateModelUpdateServer().getId(),
                mPresenter.getUpdateDetails(),
                mStartUpdate.isEnabled()));
    }
    
    private void silentRadioCheck(@NonNull RadioGroup radioGroup, int checkId) {
        radioGroup.setOnCheckedChangeListener(null);
        radioGroup.check(checkId);
        radioGroup.setOnCheckedChangeListener(this);
    }

    private static UpdateViewState restoreFromBundle(@NonNull Bundle bundle) {
        return new UpdateViewStateImpl(
                bundle.getString (UPDATE_SERVER, ""),
                bundle.getInt (UPDATE_MODEL_COMPONENT, UpdateModelComponentMode.All.getId()),
                bundle.getInt (UPDATE_MODEL_UPDATE_SERVER, UpdateModelUpdateServerMode.Random.getId()),
                bundle.getStringArrayList (UPDATE_UPDATE_DETAILS),
                bundle.getBoolean (UPDATE_START_BUTTON_STATE, true)
        );
    }

    private static void saveToBundle(@NonNull Bundle bundle, @NonNull UpdateViewState state) {
        bundle.putString (UPDATE_SERVER, state.getUpdateServer());
        bundle.putInt (UPDATE_MODEL_COMPONENT, state.getUpdateModelComponent().getId());
        bundle.putInt (UPDATE_MODEL_UPDATE_SERVER, state.getUpdateModelUpdateServer().getId());
        bundle.putStringArrayList (UPDATE_UPDATE_DETAILS, state.getUpdateDetails());
        bundle.putBoolean(UPDATE_START_BUTTON_STATE, state.getStartUpdateButtonState());
    }
}
