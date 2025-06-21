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

import lombok.Data;
import lombok.ToString;

/**
 *
 * {"status":"success","country":"China","countryCode":"CN","region":"HN","regionName":"Hunan","city":"Changsha","zip":"","lat":28.2014,"lon":112.9611,"timezone":"Asia/Shanghai","isp":"China Mobile Communications Corporation","org":"China Mobile Communications Corporation","as":"AS56047 China Mobile communications corporation","query":"36.148.177.108"}
 *
 */
@Data
@ToString
public class IpGeo {
    private String country;
    private String countryCode;
    private String region;
    private String regionName;
    private String city;
    private Double lat;
    private Double lon;
    private String timezone;
    private String isp;
    private String org;
    private String as;
    private String ip;
    private String status;
}
