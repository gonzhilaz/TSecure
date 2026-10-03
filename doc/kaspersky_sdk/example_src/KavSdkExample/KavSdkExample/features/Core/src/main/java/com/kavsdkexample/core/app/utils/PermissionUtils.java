/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import android.util.Log;

import com.kavsdkexample.core.BuildConfig;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class PermissionUtils {
    private static final String TAG = PermissionUtils.class.getSimpleName();

    private PermissionUtils() {
    }

    public static String getTreeDocumentId(Uri uri) {
        String treeDocumentId = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (DocumentsContract.isTreeUri(uri)) {
                    treeDocumentId = DocumentsContract.getTreeDocumentId(uri);
                }
            } else {
                // This can throw "IllegalArgumentException: Invalid URI"
                treeDocumentId = DocumentsContract.getTreeDocumentId(uri);
            }
        } catch (IllegalArgumentException e) {
            treeDocumentId = null;
        }

        return treeDocumentId;
    }

    @TargetApi(Build.VERSION_CODES.Q)
    public static boolean hasPermissionToAccessSdCard(@NonNull Context context, @NonNull String sdCardRootPath) {

        String volume = getVolume(context, sdCardRootPath);

        if (volume == null ) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && volume.compareTo(MediaStore.VOLUME_EXTERNAL_PRIMARY) == 0) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "hasPermission(): volume '" + volume + "' has permission");
            }
            return true;
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "hasPermission(): volume: " + volume);
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
                    Log.d(TAG, "hasPermission(): uri: " + uri.toString());
                }

                String documentId = getTreeDocumentId(uri);
                if (documentId == null) {
                    continue;
                }

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "hasPermission(): documentId: " + documentId);
                }
                final String[] set = documentId.split(":");
                if (set.length != 1) {
                    continue;
                }

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "hasPermission(): set[0]: " + set[0]);
                }
                final Locale local = Locale.getDefault();
                if (volume.compareTo(MediaStore.VOLUME_EXTERNAL_PRIMARY) == 0 &&
                        set[0].compareTo("primary") == 0 ||
                        set[0].toLowerCase(local).compareTo(volume.toLowerCase(local)) == 0) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "hasPermission(): volume '" + volume + "' has permission");
                    }
                    return true;
                }
            }
        }

        return false;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static String getVolume(@NonNull Context context, @NonNull String uri) {
        final String[] pathSet = uri.split("/");
        if (pathSet.length <= 1) {
            return null;
        }

        String directoryName = pathSet[pathSet.length - 1];
        if (directoryName.isEmpty()) {
            return null;
        }

        final Locale local = Locale.getDefault();
        String selectedVolume = null;
        directoryName = directoryName.toLowerCase(local);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Set<String> volumes = MediaStore.getExternalVolumeNames(context);
            for (String volume : volumes) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "obtainPermission(): directoryName: " + directoryName);
                    Log.d(TAG, "obtainPermission(): volume: " + volume);
                }
                if (directoryName.compareTo(volume.toLowerCase(local)) == 0) {
                    selectedVolume = volume;
                    break;
                }
            }
            if (selectedVolume == null && volumes.contains(MediaStore.VOLUME_EXTERNAL_PRIMARY)) {
                selectedVolume = MediaStore.VOLUME_EXTERNAL_PRIMARY;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StorageManager sm = context.getSystemService(StorageManager.class);
            if (sm != null) {
                StorageVolume volume = sm.getStorageVolume(new File(uri));
                if (volume != null && !volume.isPrimary() && volume.getUuid() != null && directoryName.compareTo(volume.getUuid().toLowerCase(local)) == 0) {
                    selectedVolume = volume.getUuid();
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            selectedVolume = directoryName;
        }

        return selectedVolume;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean ifSdCardNeedsPermissionToAccess (@NonNull Context context, @NonNull String uri) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            return false;
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            return true;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StorageManager sm = context.getSystemService(StorageManager.class);
            if (sm != null) {
                StorageVolume volume = sm.getStorageVolume(new File(uri));
                return volume == null || !volume.isPrimary();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // '/storage/emulated/0'
            String externalStorageDirectory = Environment.getExternalStorageDirectory().getPath();
            return !uri.startsWith(externalStorageDirectory);
        }

        return false;
    }
}