package org.okstar.platform.common.account;

import org.apache.commons.lang3.StringUtils;
import org.okstar.platform.common.asserts.OkAssert;

public class OkAccountUtils {
    public static OkAccountDefines.BindType determineBindType(String account) {
        if (StringUtils.isBlank(account)) {
            throw new IllegalArgumentException("Account cannot be blank");
        }

        String trimmed = account.trim();

        // 1. 优先判断邮箱（最严格）
        if (isValidEmail(trimmed)) {
            return OkAccountDefines.BindType.email;
        }

        // 2. 判断手机号（中国大陆 11 位 + 国际简单支持）
        if (isValidPhone(trimmed)) {
            return OkAccountDefines.BindType.phone;
        }

        // 3. 兜底为用户名
        return OkAccountDefines.BindType.username;
    }

    /**
     * 简单但实用的邮箱判断（生产环境建议用更严格的 regex 或库）
     */
    private static boolean isValidEmail(String s) {
        // 推荐使用 jakarta.validation 或 apache commons validator
        // 这里给一个平衡易读性和准确性的正则
        return s.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
        // 更严格版（可选）：参考 RFC 5322 简化版
        // "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$"
    }

    /**
     * 手机号判断（可根据业务调整）
     * 支持：中国大陆 11 位、带 +86 / 86、国际简单格式
     */
    private static boolean isValidPhone(String s) {
        // 去掉常见前缀和空格
        String cleaned = s.replaceAll("[\\s\\-\\+]", "");

        // 中国大陆手机号：1[3-9]\\d{9}
        if (cleaned.matches("^1[3-9]\\d{9}$")) {
            return true;
        }

        // 带国际区号的简单判断（长度 8~15 位，全数字）
        if (cleaned.matches("^\\d{8,15}$") && !cleaned.startsWith("0")) {
            return true;
        }

        // 可扩展：具体国家区号匹配
        // if (cleaned.startsWith("44") && cleaned.length() == 12) → 英国
        return false;
    }

}
