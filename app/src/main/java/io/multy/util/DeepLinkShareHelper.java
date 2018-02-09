/*
 * Copyright 2017 Idealnaya rabota LLC
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

    public static String getDeepLink(Context context, String qrCode) {

        ContentMetadata contentMetadata = new ContentMetadata();
        contentMetadata.addCustomMetadata(Constants.DEEP_LINK_QR_CODE, qrCode);

        BranchUniversalObject branchUniversalObject = new BranchUniversalObject()
                .setCanonicalIdentifier(Constants.DEEP_LINK_QR_CODE + "/" + qrCode)
                .setTitle(context.getString(R.string.app_name))
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setContentMetadata(contentMetadata);

        LinkProperties linkProperties = new LinkProperties()
                .addTag(qrCode)
                .addControlParameter("$desktop_url", "http://multy.io");
//                .addControlParameter("$sender_id", String.valueOf(userId))
//                .addControlParameter("$ios_url", "com.devmulty");
//                .addControlParameter("$ios_deeplink_path", Constants.DEEP_LINK_QR_CODE)
        return branchUniversalObject.getShortUrl(context, linkProperties, false);
    }

//    Post value = {"identity_id":"465073643500779705","device_fingerprint_id":"465073643483986343",
//          "session_id":"465075438197341989","tags":["bitcoin:1GLY7sDe7a6xsewDdUNA6F8CEoAxQsHV37"],"alias":"",
//          "channel":"","feature":"Share","stage":"","campaign":"","data":"{\"$og_title\":\"QR_CODE\",
//          \"$canonical_identifier\":\"QR_CODE\\\/bitcoin:1GLY7sDe7a6xsewDdUNA6F8CEoAxQsHV37\",\"$publicly_indexable\":\"true\",
//          \"QR_CODE\":\"bitcoin:1GLY7sDe7a6xsewDdUNA6F8CEoAxQsHV37\",\"$desktop_url\":\"http:\\\/\\\/multy.io\",\"source\":\"android\"}",
//          "hardware_id":"0ac4ee4d-567c-4301-b886-9ab9f9f4a03c","is_hardware_id_real":false,"brand":"Sony","model":"F5321","screen_dpi":320,
//          "screen_height":1184,"screen_width":720,"wifi":false,"ui_mode":"UI_MODE_TYPE_NORMAL","os":"Android","os_version":25,"country":"GB",
//          "language":"en","local_ip":"100.121.122.227","sdk":"android2.14.1","branch_key":"key_test_deEyCxvdjNmAzVchTM8RnjoeDqdUZsss"}

//    returned {"url":"https://zn0o.test-app.link/7kshikidwI"}
}
