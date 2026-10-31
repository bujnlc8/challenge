package site.haihui.challenge.daily;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import site.haihui.challenge.utils.WeixinProxy;

class WechatShareCodeTest {
    @ParameterizedTest
    @ValueSource(strings = {"release", "trial", "develop"})
    void sendsNumericQuestionSceneAndMatchingWechatEnvironment(String environment) {
        RestTemplate rest = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(rest).build();
        WeixinProxy proxy = new WeixinProxy();
        ReflectionTestUtils.setField(proxy, "restTemplate", rest);
        byte[] image = new byte[] {(byte) 137, 80, 78, 71, 13, 10, 26, 10};
        server.expect(requestTo("https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=test-token"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(
                content().json("{\"scene\":\"123\",\"page\":\"pages/answer/answer\",\"width\":430,\"env_version\":\""
                    + environment + "\",\"check_path\":"
                    + "release".equals(environment) + "}"))
            .andRespond(withSuccess(image, MediaType.IMAGE_PNG));
        assertArrayEquals(image, proxy.generateQuestionShareCode(123, environment, "test-token"));
        server.verify();
    }
    @ParameterizedTest
    @ValueSource(strings = {"release", "trial", "develop"})
    void appCodeUsesOnlyFixedHomeSceneAndPage(String environment) {
        RestTemplate rest = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(rest).build();
        WeixinProxy proxy = new WeixinProxy();
        ReflectionTestUtils.setField(proxy, "restTemplate", rest);
        byte[] image = new byte[] {(byte) 137, 80, 78, 71, 13, 10, 26, 10};
        server.expect(requestTo("https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=test-token"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(
                content().json("{\"scene\":\"home\",\"page\":\"pages/index/index\",\"width\":430,\"env_version\":\""
                    + environment + "\",\"check_path\":"
                    + "release".equals(environment) + "}"))
            .andRespond(withSuccess(image, MediaType.IMAGE_PNG));
        assertArrayEquals(image, proxy.generateAppShareCode(environment, "test-token"));
        server.verify();
    }

    @Test
    void invalidEnvironmentDoesNotMakeWechatRequest() {
        WeixinProxy proxy = new WeixinProxy();
        assertThrows(RuntimeException.class, () -> proxy.generateQuestionShareCode(123, "invalid", "test-token"));
    }
}
