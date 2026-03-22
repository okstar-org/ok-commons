package org.okstar.platform.common.auth;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OkOidcUtils {
    /**
     * 从 Keycloak realm URL 中提取 realm 名称
     *
     * @param url 格式如: http://localhost:9081/realms/okstar 或 /realms/okstar
     * @return realm 名称, 如果无法提取则返回 null
     */
    public static String extractRealmFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        try {
            // 处理可能是相对路径的情况
            String path;
            if (url.startsWith("http")) {
                URI uri = new URI(url);
                path = uri.getPath();
            } else {
                path = url; // 假设传入的就是路径部分
            }

            // 使用正则表达式提取 /realms/{realm} 中的 realm
            Pattern pattern = Pattern.compile("/realms/([^/?#]+)");
            Matcher matcher = pattern.matcher(path);

            if (matcher.find()) {
                return matcher.group(1);
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }
}
