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

package org.okstar.platform.common.ip;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class IpUtilsTest {

    @Test
    void isValidIP() {
        var ip = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "\n" +
                "<head>\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
                "    <meta http-equiv=\"content-style-type\" content=\"text/css\" />\n" +
                "    <meta http-equiv=\"content-script-type\" content=\"text/javascript\" />\n" +
                "    <meta http-equiv=\"content-language\" content=\"en\" />\n" +
                "    <meta http-equiv=\"pragma\" content=\"no-cache\" />\n" +
                "    <meta http-equiv=\"cache-control\" content=\"no-cache\" />\n" +
                "    <meta name=\"description\" content=\"Get my IP Address\" />\n" +
                "    <meta name=\"keywords\" content=\"ip address ifconfig ifconfig.me\" />\n" +
                "    <meta name=\"author\" content=\"\" />\n" +
                "    <link rel=\"shortcut icon\" href=\"favicon.ico\" />\n" +
                "    <link rel=\"canonical\" href=\"https://ifconfig.me/\" />\n" +
                "    <title>What Is My IP Address? - ifconfig.me</title>\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "    <link href=\"./static/styles/style.css\" rel=\"stylesheet\" type=\"text/css\">\n" +
                "    <link href=\"https://fonts.googleapis.com/css?family=Open+Sans&display=swap\" rel=\"stylesheet\">\n" +
                "</head>\n";

        boolean validIP = IpUtils.isValidIP(ip);
        log.info("validIp: {}", validIP);
    }
}