/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.view.impl.nonmvp;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import com.kavsdkexample.core.R;

import java.lang.ref.WeakReference;

/*
 * Base dialog for running time-consuming operation and showing result in Toast.
 */
public abstract class BaseAsyncOperationDialog extends DialogFragment {
    protected Context mContext;
    private Handler mHandler;
    private ProgressDialog mProgressDialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context.getApplicationContext();
    }

    /**
     * Implement this method to perform time-consuming operation
     * @return operation result message that will be shown in Toast.
     */
    protected abstract String check();

    protected void checkAndShowResult(View v) {
        /*
         * Checking can be time consuming.
         * So, show a "thinking" process dialog and process verification from a new thread
         */

        if (v != null) {
            if (!v.isEnabled()) {
                return;
            }
            v.setEnabled(false);
        }

        mProgressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.str_loading));
        new Thread(new CheckTask(mContext, mHandler, this)).start();

    }

    protected void onCheckCompleted(String result) {
    }

    protected boolean showToast() {
        return true;
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
    }

    private static class CheckTask implements Runnable {
        private final Context mContext;
        private final Handler mHandler;
        private final WeakReference<BaseAsyncOperationDialog> mDialogRef;

        CheckTask(@NonNull Context context, @NonNull Handler handler, @NonNull BaseAsyncOperationDialog dialog) {
            mContext   = context;
            mHandler   = handler;
            mDialogRef = new WeakReference<>(dialog);
        }

        @Override
        public void run() {
            BaseAsyncOperationDialog dialogInstance = mDialogRef.get();
            if (dialogInstance != null) {
                final String result = dialogInstance.check();
                final boolean showToast = dialogInstance.showToast();
                mHandler.post(() -> {
                    if (showToast) {
                        Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
                    }
                    BaseAsyncOperationDialog dialog = mDialogRef.get();
                    if (dialog != null && !dialog.isStateSaved()) {
                        dialog.dismissProgressDialog();
                        if (showToast) {
                            dialog.dismissAllowingStateLoss();
                        } else {
                            dialog.onCheckCompleted(result);
                        }
                    }
                });
            }
        }
    }
}
