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

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;

@Slf4j
public class OkWebUtil {
    private static final Logger logger = LoggerFactory.getLogger(OkWebUtil.class);

    private static HttpClient buildHttpClient() {

        //  创建请求配置信息
        RequestConfig requestConfig = RequestConfig.custom()
                // 设置连接超时时间
                .setConnectTimeout(3000)
                // 设置响应超时时间
                .setSocketTimeout(3000)
                // 设置从连接池获取链接的超时时间
                .setConnectionRequestTimeout(3000)
                .build();

        List<Header> headers = new java.util.ArrayList<>();
        headers.add(new BasicHeader("user-agent", "curl/7.81.0"));

        CloseableHttpClient client = HttpClients.custom()
                .setDefaultHeaders(headers)   // 设置默认请求头
                .setDefaultRequestConfig(requestConfig) // 设置默认配置
                .build();

        return client;
    }


    @SneakyThrows
    public static String getPublicIp() {
        return get("https://ifconfig.me/");
    }

    /**
     * 发送 Get请求
     *
     * @param url
     * @return 成功时返回数据字符串
     */
    public static String get(String url) {
        return get(URI.create(url));
    }


    public static String get(URI url) {
        try {
            HttpResponse response = doGet(url);
            if (response == null) {
                return null;
            }

            if (response.getStatusLine().getStatusCode() / 100 != 2) {
                return null;
            }

            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            return null;
        }
    }


    public static HttpResponse doGet(String url) {
        return doGet(URI.create(url));
    }

    public static HttpResponse doGet(URI url) {
        // 构造HttpClient的实例
        HttpClient client = buildHttpClient();

        // 创建GET方法的实例
        HttpGet method = new HttpGet(url.toString());
        try {
            return client.execute(method);
        } catch (Exception e) {
            log.error("Get: {}", url, e);
        } finally {
            method.releaseConnection();
        }
        return null;
    }


    /**
     * Adjust URL.
     *
     * @param url the url
     * @return the string
     */
    private String fixHttpURL(String url) {
        if (!url.startsWith("http")) {
            url = "http://" + url;
        }
        return url;
    }
}
