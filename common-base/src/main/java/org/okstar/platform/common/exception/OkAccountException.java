package org.okstar.platform.common.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * 账号异常
 */
@Setter
@Getter
public class OkAccountException extends Exception {

    /**
     * 账号 ID
     */
    private Long accountId;

    public OkAccountException(Long accountId, String message) {
        super(message);
        this.accountId = accountId;
    }
}
