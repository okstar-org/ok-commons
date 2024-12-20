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

package org.okstar.platform.common.exception;

import lombok.Getter;
import org.okstar.platform.common.exception.user.OkUserException;

@Getter
public class OkHttpException extends OkUserException {

    private String uri;
    private String path;

    public OkHttpException(String uri, String path, String message) {
        super(message);
        this.uri = uri;
        this.path = path;
    }

    public OkHttpException(String uri, String path, Throwable e) {
        super(e);
        this.uri = uri;
        this.path = path;
    }
}
