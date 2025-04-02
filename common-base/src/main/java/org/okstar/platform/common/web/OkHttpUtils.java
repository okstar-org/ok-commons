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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class OkHttpUtils {
    //默认连接超时 60 秒
    public static final int TIMEOUT_S = 60;
    private final HttpClient client;

    // 默认构造方法，设置合理的默认配置
    public OkHttpUtils() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2) // 优先使用 HTTP/2
                .connectTimeout(Duration.ofSeconds(TIMEOUT_S)) // 连接超时
                .followRedirects(HttpClient.Redirect.NORMAL) // 默认跟随重定向
                .build();
    }

    // 可自定义 HttpClient 的构造方法
    public OkHttpUtils(HttpClient client) {
        this.client = client;
    }

    /**
     * 执行 GET 请求
     *
     * @param url     请求的 URL
     * @param headers 可选的请求头 (key-value 对)
     * @return 响应体字符串
     * @throws Exception 如果请求失败
     */
    public String get(String url, Map<String, String> headers) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET();

        // 添加请求头
        if (headers != null) {
            headers.forEach(requestBuilder::header);
        }

        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        checkStatus(response);
        return response.body();
    }

    /**
     * 执行 POST 请求
     *
     * @param url     请求的 URL
     * @param body    请求体（例如 JSON 字符串）
     * @param headers 可选的请求头
     * @return 响应体字符串
     * @throws Exception 如果请求失败
     */
    public String postJSON(String url, String body, Map<String, String> headers) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(body != null ? body : ""));

        // 添加请求头
        if (headers != null) {
            headers.forEach(requestBuilder::header);
        }
        requestBuilder.header("Content-Type", "application/json"); // 默认 JSON


        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        checkStatus(response);
        return response.body();
    }

    /**
     * 执行 PUT 请求
     *
     * @param url     请求的 URL
     * @param body    请求体（例如 JSON 字符串）
     * @param headers 可选的请求头
     * @return 响应体字符串
     * @throws Exception 如果请求失败
     */
    public String put(String url, String body, Map<String, String> headers) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .PUT(HttpRequest.BodyPublishers.ofString(body != null ? body : ""));

        // 添加请求头
        if (headers != null) {
            headers.forEach(requestBuilder::header);
        } else {
            requestBuilder.header("Content-Type", "application/json"); // 默认 JSON
        }

        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        checkStatus(response);
        return response.body();
    }

    /**
     * 执行 DELETE 请求
     *
     * @param url     请求的 URL
     * @param headers 可选的请求头
     * @return 响应体字符串
     * @throws Exception 如果请求失败
     */
    public String delete(String url, Map<String, String> headers) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .DELETE();

        // 添加请求头
        if (headers != null) {
            headers.forEach(requestBuilder::header);
        }

        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        checkStatus(response);
        return response.body();
    }

    // 检查响应状态码，抛出异常如果不是 2xx
    private void checkStatus(HttpResponse<?> response) throws Exception {
        int statusCode = response.statusCode();
        if (statusCode < 200 || statusCode >= 300) {
            throw new Exception("HTTP request failed with status code: " + statusCode);
        }
    }
}