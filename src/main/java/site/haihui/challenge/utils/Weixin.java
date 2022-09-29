package site.haihui.challenge.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import site.haihui.challenge.common.constant.AppType;
import site.haihui.challenge.config.WeixinMiniAppConfig;
import site.haihui.challenge.dto.weixin.MiniMessageBody;
import site.haihui.challenge.dto.weixin.MiniResponse;
import site.haihui.challenge.dto.weixin.WeixinCode2Session;
import site.haihui.challenge.dto.weixin.WeixinDecryptData;

public class Weixin {

    private volatile static Weixin weixin;

    private volatile static Weixin qq;

    /**
     * 小程序appId
     */
    private String appId;

    /**
     * 小程序appSecret
     */
    private String appSecret;

    private AppType appType;

    /**
     * 代理
     */
    private WeixinProxy weixinProxy = (WeixinProxy) SpringContextBean.getBean("weixinProxy");

    private Weixin(String appId, String appSecret, AppType appType) {
        this.appId = appId;
        this.appSecret = appSecret;
        this.appType = appType;
    }

    public static Weixin getWeixin(AppType appType) {
        if (appType.equals(AppType.WXMINIAPP)) {
            if (weixin == null) {
                synchronized (Weixin.class) {
                    if (weixin == null) {
                        weixin = new Weixin(WeixinMiniAppConfig.AppId, WeixinMiniAppConfig.AppSecret,
                                AppType.WXMINIAPP);
                    }
                }
            }
            return weixin;
        }
        if (qq == null) {
            synchronized (Weixin.class) {
                if (qq == null) {
                    qq = new Weixin(WeixinMiniAppConfig.QQAppId, WeixinMiniAppConfig.QQAppSecret, AppType.QQMINIAPP);
                }
            }
        }
        return qq;
    }

    /**
     * code2Session
     * 
     * @param code
     * @return
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    public WeixinCode2Session code2Session(String code)
            throws JsonMappingException, JsonProcessingException {
        return weixinProxy.code2Session(code, appId, appSecret, appType);
    }

    /**
     * 解析加密数据得到对象
     * 
     * @param code
     * @param encryptedData
     * @param iv
     * @return
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    public WeixinDecryptData getWeixinDecryptData(String code, String encryptedData, String iv)
            throws JsonMappingException, JsonProcessingException {
        return weixinProxy.getWeixinDecryptData(code, encryptedData, iv, appId, appSecret, appType);
    }

    public String getAccessToken() {
        return weixinProxy.getAccessToken(appId, appSecret, appType);
    }

    public byte[] generateWxQrCodeBuffer(Integer scene, String page, Integer width)
            throws JsonMappingException, JsonProcessingException {
        String accessToken = getAccessToken();
        if (accessToken.isEmpty()) {
            return null;
        }
        return weixinProxy.generateWxQrCodeBuffer(scene, page, width, accessToken);
    }

    public MiniResponse sendMiniMessage(MiniMessageBody body) {
        String accessToken = getAccessToken();
        System.out.println(accessToken);
        if (accessToken.isEmpty()) {
            return null;
        }
        return weixinProxy.sendMiniMessage(accessToken, body);
    }
}
