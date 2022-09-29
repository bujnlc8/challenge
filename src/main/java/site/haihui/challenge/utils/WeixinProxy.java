package site.haihui.challenge.utils;

import java.security.Key;
import java.security.Security;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import site.haihui.challenge.common.constant.AppType;
import site.haihui.challenge.common.constant.Config;
import site.haihui.challenge.common.exception.BaseException;
import site.haihui.challenge.dto.weixin.AccessTokenResponse;
import site.haihui.challenge.dto.weixin.MiniMessageBody;
import site.haihui.challenge.dto.weixin.MiniResponse;
import site.haihui.challenge.dto.weixin.WeixinCode2Session;
import site.haihui.challenge.dto.weixin.WeixinDecryptData;
import site.haihui.challenge.service.IRedisService;

@Slf4j
@Component
public class WeixinProxy {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IRedisService<String> redisService;

    private static final String Code2SessionUrl = "https://api.weixin.qq.com/sns/jscode2session?appid={appId}&secret={appSecret}&js_code={code}&grant_type=authorization_code";

    private static final String AccessTokenUrl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={appId}&secret={appSecret}";

    private static final String WxacodeUrl = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token={accessToken}";

    private static final String KEY_ALGORITHM = "AES";

    private static final String ALGORITHM_STR = "AES/CBC/PKCS7Padding";

    private static Cipher cipher;

    private static Key key;

    private static final String MiniMessageUrl = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token={accessToken}";

    public static String makeUrl(String url, AppType appType) {
        if (appType.equals(AppType.WXMINIAPP)) {
            return url;
        }
        return url.replace("api.weixin.qq.com", "api.q.qq.com");
    }

    /**
     * code2Session
     *
     * @param code
     * @param appId
     * @param appSecret
     * @return
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    public WeixinCode2Session code2Session(String code, String appId, String appSecret, AppType appType) {
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("appId", appId);
        requestMap.put("appSecret", appSecret);
        requestMap.put("code", code);
        ResponseEntity<WeixinCode2Session> responseEntity = restTemplate.getForEntity(makeUrl(Code2SessionUrl, appType),
                WeixinCode2Session.class, requestMap);
        log.info("responseEntity body: {}", responseEntity.getBody());
        return responseEntity.getBody();
    }

    public WeixinDecryptData decryptData(String encryptDataB64, String sessionKeyB64, String ivB64,
            String appId) throws JsonMappingException, JsonProcessingException {
        Decoder decoder = Base64.getDecoder();
        String decryptString = new String(
                decryptOfDiyIV(
                        decoder.decode(encryptDataB64),
                        decoder.decode(sessionKeyB64),
                        decoder.decode(ivB64)));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        WeixinDecryptData weixinDecryptData = objectMapper.readValue(decryptString, WeixinDecryptData.class);
        log.info("weixinDecryptData: {}, {}", weixinDecryptData.getWatermark().getAppId().equals(appId), appId);
        // 校验appId
        if (null == weixinDecryptData || null == weixinDecryptData.getWatermark()
                || !weixinDecryptData.getWatermark().getAppId().equals(appId)) {
            Config.ResponseStatus responseStatus = Config.ResponseStatus.WeixinEncryptDataErr;
            log.error("WeixinDecryptData error occur, decryptString: {}, result: {}", decryptString, weixinDecryptData);
            throw new BaseException(responseStatus.getResponseCode(), responseStatus.getDescription());
        }
        return weixinDecryptData;
    }

    private static void init(byte[] keyBytes) {
        // 如果密钥不足16位，那么就补足
        int base = 16;
        if (keyBytes.length % base != 0) {
            int groups = keyBytes.length / base + (keyBytes.length % base != 0 ? 1 : 0);
            byte[] temp = new byte[groups * base];
            Arrays.fill(temp, (byte) 0);
            System.arraycopy(keyBytes, 0, temp, 0, keyBytes.length);
            keyBytes = temp;
        }
        Security.addProvider(new BouncyCastleProvider());
        key = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
        try {
            cipher = Cipher.getInstance(ALGORITHM_STR, "BC");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 解密方法
     *
     * @param encryptedData 要解密的字符串
     * @param keyBytes 解密密钥
     * @param ivs 自定义对称解密算法初始向量 iv
     * @return 解密后的字节数组
     */
    private static byte[] decryptOfDiyIV(byte[] encryptedData, byte[] keyBytes, byte[] ivs) {
        byte[] encryptedText = null;
        init(keyBytes);
        try {
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(ivs));
            encryptedText = cipher.doFinal(encryptedData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedText;
    }

    /**
     * 解析加密数据得到对象
     *
     * @param code
     * @param encryptedData
     * @param iv
     * @param appId
     * @param appSecret
     * @return
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    public WeixinDecryptData getWeixinDecryptData(String code, String encryptedData, String iv, String appId,
            String appSecret, AppType appType) throws JsonMappingException, JsonProcessingException, BaseException {
        WeixinCode2Session weixinCode2Session = code2Session(code, appId, appSecret, appType);
        if (null == weixinCode2Session
                || (null != weixinCode2Session.getErrcode() && weixinCode2Session.getErrcode() != 0)) {
            Config.ResponseStatus responseStatus = Config.ResponseStatus.WeixinCode2SessionErr;
            log.error("WeixinCode2Session error occur, code: {}, result: {}", code, weixinCode2Session);
            throw new BaseException(responseStatus.getResponseCode(),
                    responseStatus.getDescription() + ", " + weixinCode2Session.getErrmsg());
        }
        WeixinDecryptData decryptData = decryptData(encryptedData, weixinCode2Session.getSessionKey(), iv, appId);
        decryptData.setOpenId(weixinCode2Session.getOpenId());
        decryptData.setUnionId(weixinCode2Session.getUnionId());
        return decryptData;
    }

    public String getAccessToken(String appId, String appSecret, AppType appType) {
        String key = String.format("cached_access_token:%s", appType.getAppType());
        String cached = redisService.get(key);
        if (null != cached && !cached.isEmpty()) {
            return cached;
        }
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("appId", appId);
        requestMap.put("appSecret", appSecret);
        ResponseEntity<AccessTokenResponse> responseEntity = restTemplate.getForEntity(makeUrl(AccessTokenUrl, appType),
                AccessTokenResponse.class, requestMap);
        log.info("responseEntity body: {}", responseEntity.getBody());
        AccessTokenResponse accessTokenResponse = responseEntity.getBody();
        if (null != accessTokenResponse.getErrcode() && accessTokenResponse.getErrcode() != 0) {
            return "";
        }
        String accessToken = accessTokenResponse.getAccessToken();
        // 缓存accessToken
        redisService.set(key, accessToken, accessTokenResponse.getExpiresIn() - 200);
        return accessToken;
    }

    public byte[] generateWxQrCodeBuffer(Integer scene, String page, Integer width, String accessToken) {
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("accessToken", accessToken);
        WeixinProxy.QrCodeParam param = this.new QrCodeParam(scene, page, width);
        HttpEntity<WeixinProxy.QrCodeParam> request = new HttpEntity<>(param);
        ResponseEntity<byte[]> responseEntity = restTemplate.postForEntity(WxacodeUrl, request, byte[].class,
                requestMap);
        return responseEntity.getBody();
    }

    public MiniResponse sendMiniMessage(String accessToken, MiniMessageBody body) {
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("accessToken", accessToken);
        HttpEntity<MiniMessageBody> request = new HttpEntity<>(body);
        ResponseEntity<MiniResponse> responseEntity = restTemplate.postForEntity(MiniMessageUrl, request,
                MiniResponse.class, requestMap);
        return responseEntity.getBody();
    }

    public class QrCodeParam {

        public QrCodeParam(Integer scene, String page, Integer width) {
            this.scene = scene;
            this.page = page;
            this.width = width;
        }

        public Integer scene;

        public String page;

        public Integer width;
    }
}
