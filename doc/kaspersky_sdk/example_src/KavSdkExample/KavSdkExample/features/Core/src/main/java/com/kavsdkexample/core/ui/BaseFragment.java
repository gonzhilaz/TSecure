/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.ui;

import android.os.Bundle;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.util.Log;
import android.view.View;

import com.kavsdkexample.core.BuildConfig;
import com.kavsdkexample.core.app.presenter.BasePresenter;
import com.kavsdkexample.core.app.view.BaseView;
import com.kavsdkexample.core.app.view.BaseViewState;

import java.util.List;

public abstract class BaseFragment<VIEW      extends BaseView,
                                   VIEWSTATE extends BaseViewState,
                                   PRESENTER extends BasePresenter<VIEW, VIEWSTATE>>
                            extends Fragment implements BaseView {
    private static final String TAG = BaseFragment.class.getSimpleName();
    private boolean mViewCreated = true;
    private boolean mProcessBackPressed;

    @Override
    @CallSuper
    public void onStart() {
        super.onStart();
        //noinspection unchecked
        PRESENTER presenter = getPresenter();
        if (presenter != null) {
            presenter.subscribe((VIEW) this, getViewState(), mViewCreated);
            mViewCreated = false;
        }
    }

    @Override
    @CallSuper
    public void onPause() {
        super.onPause();
        //noinspection unchecked
        PRESENTER presenter = getPresenter();
        if (presenter != null) {
            presenter.viewPaused((VIEW) this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //noinspection unchecked
        PRESENTER presenter = getPresenter();
        if (presenter != null) {
            presenter.viewResumed((VIEW) this);
        }
    }

    @Override
    @CallSuper
    public void onStop() {
        super.onStop();
        PRESENTER presenter = getPresenter();
        if (presenter != null) {
            presenter.unsubscribe();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewCreated = true;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        PRESENTER presenter = getPresenter();
        if (presenter != null) {
            presenter.unsubscribe();
        }
    }

    @Override
    public void onSdkInited() {
    }

    protected void processBackPressed() {
        mProcessBackPressed = true;
    }

    /**
     * Could handle back press.
     * @return true if back press was handled
     */
    public boolean onBackPressed() {
        if (mProcessBackPressed) {
            FragmentManager fragmentManager = getChildFragmentManager();

            List<Fragment> fragments = fragmentManager.getFragments();
            for (Fragment fragment : fragments) {
                if (fragment.isVisible() && fragment.getUserVisibleHint() && fragment instanceof BaseFragment) {
                    boolean handled = ((BaseFragment)fragment).onBackPressed();
                    if (handled) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void showProgressDialog() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Showing progress dialog for fragment: " + getClass().getName());
        }
        ProgressDialogFragment.show(requireFragmentManager());
    }

    @Override
    public void hideProgressDialog() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Hiding progress dialog for fragment: " + getClass().getName());
        }
        ProgressDialogFragment.hide(requireFragmentManager());
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

    public String getFragmentTag() {
        return null;
    }
}
