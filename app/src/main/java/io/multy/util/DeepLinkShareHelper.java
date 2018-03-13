/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;


import android.content.Context;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.LinkProperties;
import io.multy.R;


public class DeepLinkShareHelper {

    public static String getDeepLink(Context context, String address, String amount) {

        ContentMetadata contentMetadata = new ContentMetadata();
        contentMetadata.addCustomMetadata(context.getString(R.string.address), address);
        contentMetadata.addCustomMetadata(context.getString(R.string.amount), amount);

        BranchUniversalObject branchUniversalObject = new BranchUniversalObject()
                .setTitle(context.getString(R.string.app_name))
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setContentMetadata(contentMetadata);

        LinkProperties linkProperties = new LinkProperties()
                .addTag(context.getString(R.string.bitcoin))
                .addControlParameter("$desktop_url", Constants.MULTY_IO_URL);

        return branchUniversalObject.getShortUrl(context, linkProperties, false);
    }
}
