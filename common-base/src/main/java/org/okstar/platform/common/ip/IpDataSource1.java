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

import org.okstar.platform.common.json.OkJsonUtils;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Optional;

/**
 * ip-api.com
 */
public class IpDataSource1 implements IpDataSource {
    HttpClient client;
    private final long timeout;

    public IpDataSource1(HttpClient client, long timeout) {
        this.client = client;
        this.timeout = timeout;
    }

    @Override
    public String getName() {
        return "ip-api.com";
    }

    @Override
    public String getUrl() {
        /**
         * {
         * "status":"success",
         * "country":"China",
         * "countryCode":"CN",
         * "region":"HN",
         * "regionName":"Hunan",
         * "city":"Changsha",
         * "lat":28.2014,"lon":112.9611,
         * "timezone":"Asia/Shanghai",
         * "isp":"China Mobile Communications Corporation",
         * "org":"China Mobile Communications Corporation",
         * "as":"AS56047 China Mobile communications corporation",
         * "query":"36.148.177.108"
         * }
         */
        return "http://ip-api.com/json";
    }

    @Override
    public Optional<String> doGetIp() throws IOException, InterruptedException {
        Optional<String> optional = doGet(client, timeout);
        return optional.map(res -> {
            var map = OkJsonUtils.asObject(res, HashMap.class);
            return (String.valueOf(map.getOrDefault("query", "")));
        });
    }

    @Override
    public Optional<IpGeo> doGetGeo() throws IOException, InterruptedException {
        Optional<String> optional = doGet(client, timeout);
        return optional.map(res -> {
            IpGeo geo = OkJsonUtils.asObject(res, IpGeo.class);
            var map = OkJsonUtils.asObject(res, HashMap.class);
            geo.setIp(String.valueOf(map.getOrDefault("query", "")));
            return geo;
        });
    }
}
