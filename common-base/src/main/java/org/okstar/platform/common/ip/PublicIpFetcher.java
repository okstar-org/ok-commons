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

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 公网IP获取工具类（使用SLF4J日志）
 */
@Slf4j
public class PublicIpFetcher {

    // 默认连接超时时间（秒）
    private static final long DEFAULT_CONNECT_TIMEOUT = 5;
    // 默认最大重试次数
    private static final int DEFAULT_MAX_RETRIES = 3;
    // 默认重试间隔（毫秒）
    private static final long DEFAULT_RETRY_DELAY = 200;

    // API源配置（按优先级排序）
    private final List<IpDataSource> API_SOURCES = new ArrayList<>();


    private final HttpClient sharedClient;

    // 构造函数，使用默认配置
    public PublicIpFetcher() {
        this(DEFAULT_CONNECT_TIMEOUT,
                DEFAULT_MAX_RETRIES,
                DEFAULT_RETRY_DELAY);
    }

    // 构造函数，自定义配置
    public PublicIpFetcher(long connectTimeout, int maxRetries, long retryDelay) {

        // 创建共享的HttpClient实例
        this.sharedClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(connectTimeout))
                .build();

        // 创建线程池
        API_SOURCES.add(new IpDataSource2(sharedClient, connectTimeout));
        API_SOURCES.add(new IpDataSource3(sharedClient, connectTimeout));
        API_SOURCES.add(new IpDataSource1(sharedClient, connectTimeout));
        API_SOURCES.forEach(s -> {
            log.info("使用Ip数据源: {}, {}", s.getName(), s.getUrl());
        });

    }

    /**
     * 获取公网IP地址（优先使用所有API并行请求，取最快响应）
     *
     * @return 公网IP地址，失败时返回null
     */
    public Optional<IpGeo> getPublicIpGeo() {
        log.debug("开始获取公网IP信息...");
        // 为每个API源创建异步任务
        for (IpDataSource source : API_SOURCES) {
            try {
                log.info("从数据源:`{}` 获取IP信息", source.getName());
                var r = source.doGetGeo();
                if (r.isEmpty()) {
                    log.warn("空内容!");
                    continue;
                }
                log.debug("Geo: {}", r.get());
                return r;
            } catch (IOException | InterruptedException e) {
                log.warn("查询失败:{}", e.getMessage());
            }
        }
        return Optional.empty();
    }

    public Optional<String> getPublicIpAddress() {
        // 为每个API源创建异步任务
        for (IpDataSource source : API_SOURCES) {
            try {
                log.info("从数据源:`{}` 获取IP信息", source.getName());
                var r = source.doGetIp();
                if (r.isEmpty()) {
                    log.warn("空内容!");
                    continue;
                }
                log.debug("GeoIp: {}", r.get());
                return r;
            } catch (IOException | InterruptedException e) {
                log.warn("查询失败:{}", e.getMessage());
            }
        }
        return Optional.empty();
    }


}