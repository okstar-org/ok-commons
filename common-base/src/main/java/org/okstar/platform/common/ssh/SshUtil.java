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

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.Asserts;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * SSH util
 */
@Slf4j
public class SshUtil {

    private SshUtil() {
    }

    public static Session openSession(String username, String host, int port, String prvkey) throws JSchException, IOException {
        // 设置 JSch 会话
        log.info("Open session username:{}, host:{}, port:{}", username, host, port);
        log.info("private key:{}", prvkey);

        JSch jsch = new JSch();
        jsch.addIdentity(prvkey);

        var session = jsch.getSession(username, host, port);


        // 禁用主机密钥检查（不推荐在生产环境中使用）
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        // 连接会话
        session.connect();

        return session;
    }

    public static Session openSession(String username, String password, String host, int port) throws JSchException {
        // 设置 JSch 会话
        JSch jsch = new JSch();

        var session = jsch.getSession(username, host, port);
        session.setPassword(password);

        // 禁用主机密钥检查（不推荐在生产环境中使用）
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        // 连接会话
        session.connect();

        return session;
    }

    public static void closeSession(Session session) {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    /**
     * 打开 SFTP 通道
     */
    public static ChannelSftp openSftpChannel(Session session) throws JSchException {
        Asserts.notNull(session, "session is null!");

        if (!session.isConnected()) {
            session.connect();
        }

        var channel = session.openChannel("sftp");
        return (ChannelSftp) channel;
    }


    public static ChannelExec openExecChannel(Session session) throws JSchException {
        Asserts.notNull(session, "session is null!");

        if (!session.isConnected()) {
            session.connect();
        }

        return (ChannelExec) session.openChannel("exec");
    }


    public static void closeChannel(Channel channel) {
        if (channel != null) {
            channel.disconnect();
        }
    }

    public static int writeFileOverSFTP(Session session, String remoteFilePath, String localFilePath) throws IOException, JSchException, SftpException {
        log.info("Write file:{} => {}", remoteFilePath, localFilePath);

        ChannelSftp channel;
        int bytes = 0;

        try {
            channel = openSftpChannel(session);
            channel.connect();
            try (InputStream localInputStream = SshUtil.class.getResourceAsStream(localFilePath);
                 OutputStream remoteOutputStream = channel.put(remoteFilePath)) {
                if (localInputStream != null) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = localInputStream.read(buffer)) != -1) {
                        remoteOutputStream.write(buffer, 0, bytesRead);
                        bytes += bytesRead;
                    }
                }
            } finally {
                //exit and close channel
                channel.exit();
                closeChannel(channel);
            }
        } finally {
            //close session
            closeSession(session);
        }

        return bytes;
    }


    public static int executeCommand(Session session, String command, Consumer<InputStream> input,
                                     Consumer<InputStream> err) throws JSchException, IOException {
        ChannelExec channel = null;
        try {
            log.info("Exec command: {}", command);

            // 执行命令
            channel = openExecChannel(session);

            channel.setCommand(command);
            channel.connect();

            // 读取输出
            if (input != null)
                input.accept(channel.getInputStream());
            if (err != null)
                err.accept(channel.getErrStream());
            while (!channel.isClosed()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException ignored) {

                }
            }
            int exitStatus = channel.getExitStatus();
            log.info("Exit status=> {}", exitStatus);
            return exitStatus;
        } finally {
            closeChannel(channel);
        }

    }

    public static void executeCommand(Session session, String command) throws JSchException, IOException {
        executeCommand(session, command, null, null);
    }
}
