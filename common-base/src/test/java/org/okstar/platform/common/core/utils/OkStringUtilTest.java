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

package org.okstar.platform.common.core.utils;


import org.junit.jupiter.api.Test;
import org.okstar.platform.common.string.OkNameUtil;
import org.okstar.platform.common.string.OkStringUtil;
import org.testng.Assert;

class OkStringUtilTest {

    @Test
    void combineName() {
        Assert.assertTrue("高".equals(OkNameUtil.combinePeopleName("zh", "高", null)));
        Assert.assertTrue("高".equals(OkNameUtil.combinePeopleName("zh", "高", "")));
        Assert.assertTrue("高".equals(OkNameUtil.combinePeopleName("zh", "", "高")));
        Assert.assertTrue("高".equals(OkNameUtil.combinePeopleName("zh", null, "高")));
        Assert.assertTrue("高杰".equals(OkNameUtil.combinePeopleName("zh", "高", "杰")));

        Assert.assertTrue("Bill".equals(OkNameUtil.combinePeopleName("en", "Bill", "")));
        Assert.assertTrue("Bill".equals(OkNameUtil.combinePeopleName("en", "Bill", null)));
        Assert.assertTrue("Gates".equals(OkNameUtil.combinePeopleName("en", "", "Gates")));
        Assert.assertTrue("Bill Gates".equals(OkNameUtil.combinePeopleName("en", "Bill", "Gates")));
    }


    @Test
    void checkHostAddr(){
       Assert.assertTrue(OkStringUtil.isValidHostAddr("okstar.org", false));
       Assert.assertTrue(OkStringUtil.isValidHostAddr("okstar.org.cn", false));
       Assert.assertTrue(OkStringUtil.isValidHostAddr("meet.chuanshaninf.com", false));
       Assert.assertTrue(OkStringUtil.isValidHostAddr("meet.chuanshaninf.com.cn", false));
       Assert.assertTrue(OkStringUtil.isValidHostAddr("117.111.111.1", false));
       Assert.assertTrue(OkStringUtil.isValidHostAddr("xx.w", false));
    }
}
