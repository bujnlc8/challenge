package site.haihui.challenge.utils;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import site.haihui.challenge.common.exception.CommonException;
import site.haihui.challenge.dto.weixin.WeixinDecryptData;

@Component
@Slf4j
public class Alipay {

    @Value("${alipay.rsa_public_key}")
    private String publicKey;

    @Value("${alipay.rsa_private_key}")
    private String privateKey;

    @Value("${alipay.app_id}")
    private String appId;

    private static final String url = "https://openapi.alipay.com/gateway.do";

    private AlipaySystemOauthTokenResponse getAlipayOauthTokenResponse(String authCode) throws AlipayApiException {
        AlipayClient alipayClient = new DefaultAlipayClient(url, appId,
                privateKey, "json", "utf-8", publicKey, "RSA2");
        AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
        request.setGrantType("authorization_code");
        request.setCode(authCode);
        return alipayClient.execute(request);
    }

    public WeixinDecryptData getAlipayUserInfo(String authCode) {
        try {
            AlipaySystemOauthTokenResponse response = getAlipayOauthTokenResponse(authCode);
            if (response.isSuccess()) {
                AlipayClient alipayClient = new DefaultAlipayClient(url, appId,
                        privateKey, "json", "utf-8", publicKey, "RSA2");
                AlipayUserInfoShareRequest request = new AlipayUserInfoShareRequest();
                AlipayUserInfoShareResponse response1 = alipayClient.execute(request, response.getAccessToken());
                if (response1.isSuccess()) {
                    WeixinDecryptData res = new WeixinDecryptData();
                    res.setOpenId(response1.getUserId());
                    if (!StringUtils.isBlank(response1.getGender())) {
                        res.setGender(response1.getGender().equals("F") ? 2 : 1);
                    } else {
                        res.setGender(0);
                    }
                    res.setCity(null == response1.getCity() ? "" : response1.getCity());
                    res.setCountry(null == response1.getCountryCode() ? "" : response1.getCountryCode());
                    res.setNickName(null == response1.getNickName() ? "" : response1.getNickName());
                    res.setProvince(null == response1.getProvince() ? "" : response1.getProvince());
                    res.setAvatarUrl(null == response1.getAvatar() ? "" : response1.getAvatar());
                    return res;
                }
            }
        } catch (AlipayApiException e) {
            log.error("getAlipayOauthTokenResponse error occur, %s", e);
        }
        throw new CommonException("获取用户信息失败");
    }
}
