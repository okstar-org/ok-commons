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

import org.junit.jupiter.api.Test;

import java.util.Optional;

public class PublicIpFetcherTest {

    @Test
    void getPublicIp() {
        /**
         * 主方法示例
         */
       var fetcher = new PublicIpFetcher();
        Optional<String> publicIp = fetcher.getPublicIpAddress();

        if (publicIp.isPresent()) {
            System.out.println("当前公网IP地址: " + publicIp.get());
        } else {
            System.err.println("获取公网IP失败，请检查网络连接");
        }
    }
}