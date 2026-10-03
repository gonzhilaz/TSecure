/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_connectivity.nonmvp;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import com.kavsdkexample.core.app.utils.IoUtils;
import com.kavsdkexample.secure_connectivity.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

public class SecureConnectionTrustIpListActivity extends ListActivity {

    static final String EXTRA_TRUSTED_URLS = "trusted_urls";

    private static final String TRUSTED_URLS_FILENAME = Environment.getExternalStorageDirectory() + "/trusted_urls";
    private static final File TRUSTED_URLS_FILE = new File(TRUSTED_URLS_FILENAME);

    private ArrayList<String> mTrustedUrls;
    private EditText mNewIpEditText;

    public static Intent getIntent(Context context, ArrayList<String> trustedUrls) {
        Intent i = new Intent(context, SecureConnectionTrustIpListActivity.class);
        i.putExtra(EXTRA_TRUSTED_URLS, trustedUrls);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secure_connection_trustip_listactivity);
        initControl();
    }

    public static void saveTrustedUrls(ArrayList<String> urls) {
        ObjectOutputStream out = null;
        try {
            FileOutputStream fos = new FileOutputStream(TRUSTED_URLS_FILE);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            out = new ObjectOutputStream(bos);
            out.writeObject(urls);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IoUtils.closeQuietly(out);
        }
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<String> loadTrustedUrls() {
        ObjectInputStream in = null;
        try {
            FileInputStream fos = new FileInputStream(TRUSTED_URLS_FILE);
            BufferedInputStream bos = new BufferedInputStream(fos);
            in = new ObjectInputStream(bos);
            return (ArrayList<String>) in.readObject();
        } catch (OptionalDataException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IoUtils.closeQuietly(in);
        }
        return new ArrayList<>();
    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {

        if (KeyEvent.KEYCODE_BACK == keyCode) {
            Intent i = new Intent();
            i.putExtra(EXTRA_TRUSTED_URLS, mTrustedUrls);
            setResult(RESULT_OK, i);
        }

        return super.onKeyDown(keyCode, event);
    }

    private void initControl() {

        mNewIpEditText = findViewById(R.id.newIpText);
        Button btn = findViewById(R.id.addIpButton);
        btn.setOnClickListener(v -> doAddItem());
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mTrustedUrls = extras.getStringArrayList(EXTRA_TRUSTED_URLS);
        }
        if (mTrustedUrls == null) {
            mTrustedUrls = new ArrayList<>();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, mTrustedUrls);
        setListAdapter(adapter);
        getListView().setOnItemLongClickListener((parent, v, position, id) -> {
            doRemoveItem(position);
            return true;
        });

    }

    @SuppressWarnings("unchecked")
    protected void doAddItem() {

        String ip = mNewIpEditText.getText().toString();
        if (NetUtils.isValidIPv4(ip)) {

            mTrustedUrls.add(ip);
            saveTrustedUrls(mTrustedUrls);
            mNewIpEditText.setText("");
            ((ArrayAdapter<String>) getListAdapter()).notifyDataSetChanged();

        }
    }

    protected void doRemoveItem(final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.str_secureconnection_msg_delete_selected_item);
        builder.setNegativeButton(android.R.string.no, null);
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            mTrustedUrls.remove(position);
            saveTrustedUrls(mTrustedUrls);
            //noinspection unchecked
            ((ArrayAdapter<String>) SecureConnectionTrustIpListActivity.this.getListAdapter()).notifyDataSetChanged();

        });
        builder.create().show();

    }
}
