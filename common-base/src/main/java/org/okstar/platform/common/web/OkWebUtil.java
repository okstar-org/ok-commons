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
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.okstar.platform.common.date.OkDateUtils;
import org.okstar.platform.common.string.OkStringUtil;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class OkWebUtil {

    private static HttpClient buildHttpClient(URI url) {
        try {
            //  创建请求配置信息
            RequestConfig requestConfig = RequestConfig.custom()
                    // 设置连接超时时间
                    .setConnectTimeout(60 * 1000)
                    // 设置响应超时时间
                    .setSocketTimeout(60 * 1000)
                    // 设置从连接池获取链接的超时时间
                    .setConnectionRequestTimeout(3000)
                    .build();

            List<Header> headers = new ArrayList<>();

            // return HttpClients.createDefault();

            var builder = HttpClients.custom().setDefaultHeaders(headers).setDefaultRequestConfig(requestConfig);

            // 信任所有
            SSLContext sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, (chain, authType) -> true)
                    .build();

            return builder.setSSLContext(sslContext).setSSLHostnameVerifier((hostname, sslSession) -> {
                log.info("Verify host:{}", hostname);
                return true;
            }).build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @SneakyThrows
    public static String getPublicIp() {
        return get("https://ifconfig.me/");
    }

    /**
     * 返回正确返回码+存在内容
     * @param url
     * @return
     */
    public static boolean hasOkContent(String url) {
        try {
            String content = get(url + "?t=" + OkDateUtils.now().getTime());
            return OkStringUtil.isNotEmpty(content);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
            var response = doGet(url);
            int code = response.getStatusLine().getStatusCode() / 100;
            if (!(code == 2 || code == 3)) {
                return null;
            }
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static HttpResponse doGet(String url) throws IOException {
        return doGet(URI.create(url));
    }

    public static HttpResponse doGet(URI url) throws IOException {
        log.info("doGet url:{}", url);
        // 构造HttpClient的实例
        HttpClient client = buildHttpClient(url);
        // 创建GET方法的实例
        HttpGet method = new HttpGet(url.toString());
        return client.execute(method);
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
