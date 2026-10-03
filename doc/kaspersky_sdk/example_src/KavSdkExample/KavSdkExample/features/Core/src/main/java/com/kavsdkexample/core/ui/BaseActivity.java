/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.kavsdkexample.core.BuildConfig;
import com.kavsdkexample.core.app.presenter.BasePresenter;
import com.kavsdkexample.core.app.view.BaseView;
import com.kavsdkexample.core.app.view.BaseViewState;

import java.util.List;

public abstract class BaseActivity<VIEW      extends BaseView,
                                   VIEWSTATE extends BaseViewState,
                                   PRESENTER extends BasePresenter<VIEW, VIEWSTATE>>
                              extends     AppCompatActivity
                              implements  BaseView {
    private static final String TAG = BaseActivity.class.getSimpleName();
    private boolean mSubscribed;
    private boolean mActivityCreated;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mActivityCreated = true;
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        subscribePresenter();
    }

    @Override
    public void onSdkInited() {
    }

    private void subscribePresenter() {
        if (!mSubscribed) {
            //noinspection unchecked
            getPresenter().subscribe((VIEW) this, getViewState(), mActivityCreated);
            mActivityCreated = false;
            mSubscribed      = true;
        }
    }

    @Override
    public void showProgressDialog() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Showing progress dialog for activity: " + getClass().getName());
        }
        ProgressDialogFragment.show(getSupportFragmentManager());
    }

    protected abstract PRESENTER getPresenter();
    protected abstract VIEWSTATE getViewState();

    @Override
    public void hideProgressDialog() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Hiding progress dialog for activity: " + getClass().getName());
        }
        ProgressDialogFragment.hide(getSupportFragmentManager());
    }

    @Override
    public void onBackPressed() {

        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();

        boolean handled = false;
        for (Fragment fragment : fragmentList) {
            if (fragment.isVisible() && fragment.getUserVisibleHint() && fragment instanceof BaseFragment) {
                handled = ((BaseFragment)fragment).onBackPressed();

                if (handled) {
                    break;
                }
            }
        }

        if (!handled) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        subscribePresenter();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //noinspection unchecked
        getPresenter().viewPaused((VIEW) this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        subscribePresenter();
        //noinspection unchecked
        getPresenter().viewResumed((VIEW) this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getPresenter().unsubscribe();
        mSubscribed = false;
    }

    @Override
    @CallSuper
    protected void onStop() {
        super.onStop();
        getPresenter().unsubscribe();
        mSubscribed = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppWatcher.watch(this);
    }
}
