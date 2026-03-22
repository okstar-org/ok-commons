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

package org.okstar.platform.common.rpc;

import lombok.*;

/**
 * Rpc返回值
 * @param <T>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcResult<T> {
    enum Type {
        DATA,       //数据
        FAILURE,    //失败
        EXCEPTION   //异常
    }
    //是否成功
    private boolean success;
    //消息
    private String msg;
    //数据
    private T data;
    //返回类型
    private Type type;

    /**
     * 返回成功实体
     * @param data
     * @return
     * @param <T>
     */
    public static <T> RpcResult<T> success(T data){
      return RpcResult.<T>builder()
              .success(true)
              .type(Type.DATA)
              .data(data).build();
    }

    /**
     * 构建异常实体
     * @param e
     * @return
     * @param <T>
     */
    public static <T> RpcResult<T> exception(Exception e){
        return RpcResult.<T>builder()
                .success(false)
                .msg(e.getMessage())
                .type(Type.EXCEPTION)
                .build();
    }

    /**
     * 构建失败实体
     * @param msg
     * @return
     * @param <T>
     */
    public static <T> RpcResult<T> exception(String msg){
        return RpcResult.<T>builder()
                .success(false)
                .msg(msg)
                .type(Type.FAILURE)
                .build();
    }
}
