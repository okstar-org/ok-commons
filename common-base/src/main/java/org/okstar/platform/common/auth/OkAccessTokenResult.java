package org.okstar.platform.common.auth;

import lombok.*;

/**
 * 登录返回信息
 * <p>
 * export interface Token {
 * access_token: string;
 * refresh_token: string;
 * token?: string;
 * token_type?: string;
 * expires_in?: number;
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OkAccessTokenResult {

    private String username;

    //tokenType："Bearer"
    protected String tokenType;
    protected String accessToken;

    /**
     * 过期时间：
     * 以秒为单位或描述的时间跨度字符串表示。
     * 如：60，”2 days”，”10h”，”7d”
     */
    private Long expiresIn;

    //refresh
    private String refreshToken;

    //refresh expires in
    private Long refreshExpiresIn;

    /**
     * session
     */
    private String session_state;

    /**
     * 地区、语言
     */
    private String locale;

}
