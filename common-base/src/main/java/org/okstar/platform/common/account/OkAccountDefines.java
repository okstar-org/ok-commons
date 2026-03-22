package org.okstar.platform.common.account;

import lombok.Getter;

/**
 * 账号相关定义
 */
public interface OkAccountDefines {

    /**
     * 账号绑定类型
     */
    @Getter
    enum BindType {
        /**
         * 用户名
         */
        username,
        /**
         * 邮箱
         */
        email,
        /**
         * 手机号码
         */
        phone
    }

}
