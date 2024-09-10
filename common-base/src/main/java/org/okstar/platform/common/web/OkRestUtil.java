/*
 * * Copyright (c) 2022 船山信息 chuanshaninfo.com
 * OkStack is licensed under Mulan PubL v2.
 * You can use this software according to the terms and conditions of the Mulan
 * PubL v2. You may obtain a copy of Mulan PubL v2 at:
 *          http://license.coscl.org.cn/MulanPubL-2.0
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PubL v2 for more details.
 * /
 */

package org.okstar.platform.common.web;


import org.okstar.platform.common.web.auth.AuthenticationToken;
import org.okstar.platform.common.web.rest.transport.ClientFactory;

public class OkRestUtil {

    private static OkRestUtil util;
    private final ClientFactory clientFactory;

    private OkRestUtil(String url, AuthenticationToken authenticationToken) {
        clientFactory = new ClientFactory(url, authenticationToken);
    }

    public static synchronized OkRestUtil getInstance(String url, AuthenticationToken authenticationToken) {
        if (util == null) {
            util = new OkRestUtil(url, authenticationToken);
        }
        return util;
    }

    public static OkRestUtil getInstance(String url) {
        if (util == null) {
            util = new OkRestUtil(url, null);
        }
        return util;
    }


    public ClientFactory getClientFactory() {
        return clientFactory;
    }


}
