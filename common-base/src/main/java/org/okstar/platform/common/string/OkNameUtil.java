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

package org.okstar.platform.common.string;

public class OkNameUtil {

    /**
     * 分割中文名字到姓和名
     *
     * @param name
     * @return UserName
     */

    public static UserName splitName(String language, String name) {
        if (OkStringUtil.isBlank(name)) {
            return UserName.builder().build();
        }

        if (isDefaultLanguage(language)) {
            if (name.length() == 2) {
                return UserName.builder()
                        .firstName(String.valueOf(name.charAt(0)))
                        .lastName(String.valueOf(name.charAt(1)))
                        .build();
            }

            if (name.length() == 3) {
                return UserName.builder()
                        .firstName(String.valueOf(name.charAt(0)))
                        .lastName(String.valueOf(new char[]{name.charAt(1), name.charAt(2)}))
                        .build();
            }

            if (name.length() == 4) {
                return UserName.builder().firstName(String.valueOf(new char[]{name.charAt(0), name.charAt(1)}))
                        .lastName(String.valueOf(new char[]{name.charAt(2), name.charAt(3)}))
                        .build();
            }
        } else {
            String[] split = OkStringUtil.split(name, " ");
            if (split.length == 1) {
                return UserName.builder().firstName(name).build();
            } else {
                return UserName.builder().firstName(split[0]).lastName(split[1]).build();
            }
        }

        return UserName.builder().firstName(name).build();
    }


    /**
     * <pre>
     * 组合人的名称
     * CJK(中日韩)=> firstName+lastName
     * Other => firstName+' '+lastName
     * </pre>
     *
     * @param language
     * @param firstName
     * @param lastName
     * @return
     */
    public static String combinePeopleName(String language, String firstName, String lastName) {
        if (OkStringUtil.isEmpty(firstName) && OkStringUtil.isEmpty(lastName))
            return "";

        if (OkStringUtil.isEmpty(firstName)) {
            return lastName;
        }

        if (OkStringUtil.isEmpty(lastName))
            return firstName;

        String fmt;
        if (isDefaultLanguage(language)) {
            //中日韩名字无需空格
            fmt = "%s%s";
        } else {
            //其他都加空格
            fmt = "%s %s";
        }
        return String.format(fmt, firstName, lastName);
    }

    /**
     * 默认语言
     *
     * @param language
     * @return
     */
    private static boolean isDefaultLanguage(String language) {
        return OkStringUtil.isEmpty(language)
                || language.startsWith("zh")
                || language.startsWith("ko")
                || language.startsWith("ja");
    }
}
