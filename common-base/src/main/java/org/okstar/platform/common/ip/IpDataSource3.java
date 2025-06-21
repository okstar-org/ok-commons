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

package org.okstar.platform.common.ip;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.Optional;

/**
 * https://icanhazip.com/
 */
public class IpDataSource3 implements IpDataSource {
    HttpClient client;
    private final long timeout;

    public IpDataSource3(HttpClient client, long timeout) {
        this.client = client;
        this.timeout = timeout;
    }

    @Override
    public String getName() {
        return "icanhazip.com";
    }

    @Override
    public String getUrl() {
        /**
         * "36.148.177.108"
         */
        return "http://icanhazip.com/";
    }

    @Override
    public Optional<String> doGetIp() throws IOException, InterruptedException {
        return doGet(client, timeout);
    }

    @Override
    public Optional<IpGeo> doGetGeo() throws IOException, InterruptedException {
        Optional<String> optional = doGet(client, timeout);
        return optional.map(res ->{
            IpGeo geo = new IpGeo();
            geo.setIp(res);
            return geo;
        });
    }
}
