/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.utils;

import android.content.res.AssetFileDescriptor;
import androidx.annotation.Keep;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Keep
public final class IoUtils {
    private static final int BUFFER_SIZE = 16 * 1024;

    private IoUtils() {
    }

    public static String toString(InputStream is) throws IOException {
        //noinspection CharsetObjectCanBeUsed
        return new String(toBytes(is), "UTF-8");
    }

    @SuppressWarnings("WeakerAccess")
    public static byte[] toBytes(InputStream is) throws IOException {
        if (is == null) {
            throw new IllegalArgumentException("is == null");
        }

        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        try {
            copy(is, result);
            return result.toByteArray();
        } finally {
            closeQuietly(result);
        }
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buff = new byte[BUFFER_SIZE];
        int len;
        while ((len = in.read(buff)) > 0) {
            out.write(buff, 0, len);
        }
    }

    public static void closeQuietly(Closeable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (IOException e) {
            //ignore
        }
    }

    public static void closeQuietly(AssetFileDescriptor c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (IOException e) {
            //ignore
        }
    }


    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }
}
