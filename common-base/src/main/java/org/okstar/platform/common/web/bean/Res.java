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

package org.okstar.platform.common.web.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * 响应信息主体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Res<T> extends DTO {

    private static final int OK = 0;
    private static final int CREATED = 1;
    private static final int ERROR = -1;

    private int code;
    private String msg;
    private T data;

    private final Map<String, Object> extra = new LinkedHashMap<>();


    /**
     * 处理成功，使用200返回码
     *
     * @param <T>
     * @return
     */
    public static <T> Res<T> ok() {
        return build(null, OK, null);
    }

    public static <T> Res<T> ok(T data) {
        return build(data, OK, null);
    }


    public static <T> Res<T> ok(T data, String msg) {
        return build(data, OK, msg);
    }


    /**
     * 创建成功，使用201返回码
     *
     * @param <T>
     * @return
     */
    public static <T> Res<T> created() {
        return build(null, CREATED, null);
    }

    public static <T> Res<T> created(T data) {
        return build(data, CREATED, null);
    }

    public static <T> Res<T> created(T data, String msg) {
        return build(null, CREATED, msg);
    }

    public static <T> Res<T> error() {
        return build(null, ERROR, null);
    }

    public static <T> Res<T> error(String msg) {
        return build(null, ERROR, msg);
    }

    public static <T> Res<T> build(T data, int code, String msg) {
        Res<T> res = new Res<>();
        res.setCode(code);
        res.setData(data);
        res.setMsg(msg);
        return res;
    }

    public void putExtra(String k, Object o) {
        extra.put(k, o);
    }

    public boolean getSuccess() {
        return code == OK || code == CREATED;
    }

}
