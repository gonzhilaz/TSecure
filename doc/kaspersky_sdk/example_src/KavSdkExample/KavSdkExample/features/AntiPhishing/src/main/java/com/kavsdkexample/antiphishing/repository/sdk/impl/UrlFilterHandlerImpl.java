/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antiphishing.repository.sdk.impl;

import android.content.Context;
import android.content.res.Resources;

import com.kaspersky.components.urlchecker.UrlInfo;
import com.kaspersky.components.urlfilter.UrlFilterHandler;
import com.kavsdkexample.antiphishing.R;
import com.kavsdkexample.core.app.utils.IoUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Demonstration of how to implement a {@link UrlFilterHandler}.
 */
public class UrlFilterHandlerImpl implements UrlFilterHandler {
    /* Application context */
    private final Context mContext;

    UrlFilterHandlerImpl(Context context) {
        mContext = context;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public InputStream getBlockPageData(String url, UrlInfo urlInfo) {
        // Reading the blocking page from resources
        Resources resources = mContext.getResources();
        InputStream page = resources.openRawResource(R.raw.permission_denied);

        ByteArrayOutputStream baos = null;
        BufferedReader reader = null;
        try {
            baos = new ByteArrayOutputStream();
            reader = new BufferedReader(new InputStreamReader(page, Charset.defaultCharset()));

            // We do not store all localized pages
            // and replace the phrases in the page by the needed ones
            // You can load localized raw resources instead
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("${BLOCK_PAGE_TITLE}")) {
                    line = line.replace("${BLOCK_PAGE_TITLE}",
                            resources.getString(R.string.str_webfilter_app_name));
                } else if (line.contains("${BLOCK_PAGE_HEADER}")) {
                    line = line.replace("${BLOCK_PAGE_HEADER}", "Block title");
                } else if (line.contains("${BLOCK_PAGE_TEXT}")) {
                    line = line.replace("${BLOCK_PAGE_TEXT}", "Block text");
                } else if (line.contains("${BLOCK_PAGE_URL}")) {
                    line = line.replace("${BLOCK_PAGE_URL}", url);
                } else if (line.contains("${ICON_FILE}")) {
                } else if (line.contains("${BLOCK_PAGE_ADD_EXCLUSION}")) {
                    line = line.replace("${BLOCK_PAGE_ADD_EXCLUSION}",
                            "Add exclusion");
                }
                baos.write(line.getBytes(Charset.defaultCharset()));
            }
            page = new ByteArrayInputStream(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IoUtils.closeQuietly(reader);
            IoUtils.closeQuietly(baos);
        }
        return page;
    }
}
