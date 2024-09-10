package org.okstar.platform.common.web;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.okstar.platform.common.web.rest.transport.RestClient;
import org.testng.Assert;

@Slf4j
class OkRestUtilTest {

    static OkRestUtil instance;

    @BeforeAll
    static void getInstance() {
        instance = OkRestUtil.getInstance("https://cloud.okstar.org.cn");
    }

    @Test
    void testGet() {
        RestClient client = instance.getClientFactory().createClient();
        var node = client.get("/api/open/federal/.well-known/info.json", String.class, null);
        log.info("node: {}", node);
        Assert.assertNotNull(node);
    }
}