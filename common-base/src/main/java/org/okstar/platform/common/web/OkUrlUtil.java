package org.okstar.platform.common.web;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * URL - Util
 */
public class OkUrlUtil {

    public static String extractBaseUrl(String urlString) {
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        int port = url.getPort();
            if (port != -1) {
                return url.getProtocol() + "://" + url.getHost() + ":" + port;
            } else {
                return url.getProtocol() + "://" + url.getHost();
            }

    }
}
