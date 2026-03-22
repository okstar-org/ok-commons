package org.okstar.platform.common.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 用户上下文工具类
 * 用于在同一个线程中传递用户信息（如操作人ID）
 */
public class OkRequestContext {

    // 使用ThreadLocal存储用户信息
    private static final ThreadLocal<Map<String, Object>> CONTEXT = new ThreadLocal<>();

    // 操作人
    private static final String KEY_OPERATOR_ID = "OPERATOR_ID";

    // 操作人IP地址
    private static final String KEY_OPERATOR_IP_ADDRESS = "OPERATOR_IP_ADDRESS";

    /**
     * 初始化上下文
     */
    private static Map<String, Object> getContextMap() {
        Map<String, Object> map = CONTEXT.get();
        if (map == null) {
            map = new HashMap<>();
            CONTEXT.set(map);
        }
        return map;
    }



    /**
     * 获取操作人ID
     */
    public static Long getOperatorId() {
        return Optional.ofNullable(getContextMap().get(KEY_OPERATOR_ID))
                .map(value -> (Long) value)
                .orElse(0L);
    }

    /**
     * 获取操作人ID（如果不存在则抛出异常）
     */
    public static Long getRequiredOperatorId() {
        Long creatorId = getOperatorId();
        if (creatorId == 0L) {
            throw new IllegalStateException("无法获取操作人ID，请确保已登录");
        }
        return creatorId;
    }

    /**
     * 设置IP地址
     */
    public static void setIpAddress(String ipAddress) {
        getContextMap().put(KEY_OPERATOR_IP_ADDRESS, ipAddress);
    }

    /**
     * 获取IP地址
     */
    public static String getIpAddress() {
        return (String) getContextMap().get(KEY_OPERATOR_IP_ADDRESS);
    }

    /**
     * 设置多个用户信息
     */
    public static void setOperatorId(Long creatorId) {
        Map<String, Object> map = getContextMap();
        map.put(KEY_OPERATOR_ID, creatorId);
    }

    /**
     * 获取所有上下文信息
     */
    public static Map<String, Object> getAll() {
        return new HashMap<>(getContextMap());
    }

    /**
     * 清空上下文（在请求结束后调用）
     */
    public static void clear() {
        CONTEXT.remove();
    }
  
}