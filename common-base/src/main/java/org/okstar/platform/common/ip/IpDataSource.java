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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

/**
 * IP数据源
 */
public interface IpDataSource {

    String getName();

    String getUrl();

    Optional<IpGeo> doGetGeo() throws IOException, InterruptedException;
    Optional<String> doGetIp() throws IOException, InterruptedException;

    default Optional<String> doGet(HttpClient sharedClient, long connectTimeout) throws IOException, InterruptedException {
        // 构建HTTP请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getUrl()))
                .timeout(Duration.ofSeconds(connectTimeout))
                .header("User-Agent", "curl/8.5.0")
                .GET()
                .build();
        // 发送请求并获取响应
        HttpResponse<String> response = sharedClient.send(request, HttpResponse.BodyHandlers.ofString());
        // 检查响应状态码
        if (response.statusCode() == 200) {
            return Optional.of(response.body());
        } else {
            return Optional.empty();
        }
    }
}
