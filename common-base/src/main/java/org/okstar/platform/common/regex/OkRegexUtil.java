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

package org.okstar.platform.common.regex;

import org.okstar.platform.common.string.OkStringUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OkRegexUtil
{
    public final static Pattern GROUP_VAR = Pattern.compile("\\$(\\d+)");

    /**
     * 正则中需要被转义的关键字
     */
    public final static Set<Character> RE_KEYS = new HashSet<>(
            Arrays.asList('$', '(', ')', '*', '+', '.', '[', ']', '?', '\\', '^', '{', '}', '|'));
 
    /**
     * 取得内容中匹配的所有结果
     *
     * @param <T> 集合类型
     * @param pattern 编译后的正则模式
     * @param content 被查找的内容
     * @param group 正则的分组
     * @param collection 返回的集合类型
     * @return 结果集
     */
    public static <T extends Collection<String>> T findAll(Pattern pattern, CharSequence content, int group,
            T collection)
    {
        if (null == pattern || null == content)
        {
            return null;
        }

        if (null == collection)
        {
            throw new NullPointerException("Null collection param provided!");
        }

        final Matcher matcher = pattern.matcher(content);
        while (matcher.find())
        {
            collection.add(matcher.group(group));
        }
        return collection;
    }

    /**
     * 转义字符，将正则的关键字转义
     *
     * @param c 字符
     * @return 转义后的文本
     */
    public static String escape(char c)
    {
        final StringBuilder builder = new StringBuilder();
        if (RE_KEYS.contains(c))
        {
            builder.append('\\');
        }
        builder.append(c);
        return builder.toString();
    }

    /**
     * 转义字符串，将正则的关键字转义
     *
     * @param content 文本
     * @return 转义后的文本
     */
    public static String escape(CharSequence content)
    {
        if (OkStringUtil.isBlank(content))
        {
            return OkStringUtil.EMPTY;
        }

        final StringBuilder builder = new StringBuilder();
        int len = content.length();
        char current;
        for (int i = 0; i < len; i++)
        {
            current = content.charAt(i);
            if (RE_KEYS.contains(current))
            {
                builder.append('\\');
            }
            builder.append(current);
        }
        return builder.toString();
    }

}
