/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.ui;

import android.os.Bundle;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.kavsdkexample.core.app.presenter.BasePresenter;
import com.kavsdkexample.core.app.view.BaseView;
import com.kavsdkexample.core.app.view.BaseViewState;

public abstract class BaseDialogFragment<VIEW      extends BaseView,
                                         VIEWSTATE extends BaseViewState,
                                         PRESENTER extends BasePresenter<VIEW, VIEWSTATE>>
                            extends DialogFragment implements BaseView {
    private boolean mFragmentCreated = true;

    @Override
    @CallSuper
    public void onStart() {
        super.onStart();
        //noinspection unchecked
        getPresenter().subscribe((VIEW) this, getViewState(), mFragmentCreated);
        mFragmentCreated = false;
    }

    @Override
    @CallSuper
    public void onPause() {
        super.onPause();
        //noinspection unchecked
        getPresenter().viewPaused((VIEW)this);
    }

    @Override
    public void onResume() {
        super.onResume();
        //noinspection unchecked
        getPresenter().viewResumed((VIEW)this);
    }

    @Override
    @CallSuper
    public void onStop() {
        super.onStop();
        getPresenter().unsubscribe();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        getPresenter().unsubscribe();
    }

    @Override
    public void onSdkInited() {
    }

    /**
     * Could handle back press.
     * @return true if back press was handled
     */
    @SuppressWarnings("unused")
    public boolean onBackPressed() {
        return false;
    }

    @NonNull
    protected abstract PRESENTER getPresenter();

    @Nullable
    protected abstract VIEWSTATE getViewState();

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppWatcher.watch(this);
    }
}
