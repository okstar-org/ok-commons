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

package org.okstar.platform.common.ssh;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
class SshUtilTest {

    String username = "gaojie";
    String password = "admin";
    String host = "localhost";

    @Test
    void openSession() throws JSchException, IOException {
        Session session = SshUtil.openSession(username, password, host, 22);
        assertNotNull(session);
    }


    @Test
    public void testRemoteCommand() throws JSchException, IOException {
        Session session = SshUtil.openSession(username, password, host, 22);
        int exitStatus = SshUtil.executeCommand(session, "ls /", null, null);
        log.info("exit status=>{}", exitStatus);
        SshUtil.closeSession(session);
    }
}