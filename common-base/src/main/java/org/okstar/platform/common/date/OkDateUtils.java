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

package org.okstar.platform.common.date;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.lang.management.ManagementFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 时间工具类
 *
 *
 */
public class OkDateUtils extends DateUtils {

    public final static String YYYYMMDD = "yyyy/MM/dd";
    public final static String YYYY_MM_DD = "yyyy-MM-dd";
    public final static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public final static String YYYYsMMsDD = "yyyy/MM/dd";
    public final static String YYYYsMMsDD_HH_MM_SS = "yyyy/MM/dd HH:mm:ss";
    public final static String YYYYdMMdDD = "yyyy.MM.dd";
    public final static String YYYYdMMdDD_HH_MM_SS = "yyyy.MM.dd HH:mm:ss";

    public enum Format {
        YYYYMMDD,
        YYYY_MM_DD,
        YYYY_MM_DD_HH_MM_SS,
        YYYYsMMsDD,
        YYYYsMMsDD_HH_MM_SS,
        YYYYdMMdDD,
        YYYYdMMdDD_HH_MM_SS
    }

    private static final String[] parsePatterns = {//
            YYYYMMDD, //
            YYYY_MM_DD,//
            YYYY_MM_DD_HH_MM_SS,    //
            YYYYsMMsDD,//
            YYYYsMMsDD_HH_MM_SS,    //
            YYYYdMMdDD,//
            YYYYdMMdDD_HH_MM_SS     //
    };

    /**
     * 获取当前Date型日期
     *
     * @return Date() 当前日期
     */
    public static Date now() {
        return new Date();
    }

    /**
     * LocalDateTime
     *
     * @return
     */
    public static LocalDateTime nowLocal() {
        return LocalDateTime.now();
    }

    /**
     * Instant
     *
     * @return Instant
     */
    public static Instant instant() {
        return Instant.now();
    }

    public static long nowTimestamp() {
        return now().getTime();
    }

    /**
     * 解析，支持多种格式
     */
    public static Date parse(String str) {
        if (str == null) {
            return null;
        }
        try {
            return parseDate(str, parsePatterns);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解析，特定格式
     * @param date
     * @param format
     * @return
     */
    public static Date parse(String date, final Format format) {
        if (date == null)
            return null;

        if (format != null) {
            try {
                switch (format) {
                    case YYYYMMDD:
                        return parseDate(date, YYYYMMDD);
                    case YYYY_MM_DD:
                        return parseDate(date, YYYY_MM_DD);
                    case YYYY_MM_DD_HH_MM_SS:
                        return parseDate(date, YYYY_MM_DD_HH_MM_SS);
                    case YYYYdMMdDD:
                        return parseDate(date, YYYYdMMdDD);
                    case YYYYdMMdDD_HH_MM_SS:
                        return parseDate(date, YYYYdMMdDD_HH_MM_SS);
                    case YYYYsMMsDD:
                        return parseDate(date, YYYYsMMsDD);
                    case YYYYsMMsDD_HH_MM_SS:
                        return parseDate(date, YYYYsMMsDD_HH_MM_SS);
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        return parse(date);
    }


    /**
     * 格式化
     */
    public static String format(Date date, Format format) {
        if (date == null)
            return "";

        if (format != null) {
            switch (format) {
                case YYYYMMDD:
                    return DateFormatUtils.format(date, YYYYMMDD);
                case YYYY_MM_DD:
                    return DateFormatUtils.format(date, YYYY_MM_DD);
                case YYYY_MM_DD_HH_MM_SS:
                    return DateFormatUtils.format(date, YYYY_MM_DD_HH_MM_SS);
                case YYYYdMMdDD:
                    return DateFormatUtils.format(date, YYYYdMMdDD);
                case YYYYdMMdDD_HH_MM_SS:
                    return DateFormatUtils.format(date, YYYYdMMdDD_HH_MM_SS);
                case YYYYsMMsDD:
                    return DateFormatUtils.format(date, YYYYsMMsDD);
                case YYYYsMMsDD_HH_MM_SS:
                    return DateFormatUtils.format(date, YYYYsMMsDD_HH_MM_SS);
            }
        }

        return DateFormatUtils.format(date, YYYY_MM_DD_HH_MM_SS);
    }


    /**
     * 获取服务器启动时间
     */
    public static Date getServerStartDate() {
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        return new Date(time);
    }

}
