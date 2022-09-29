package site.haihui.challenge.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@Data
public class WeixinMiniAppConfig implements InitializingBean {

    @Value("${weixin.app_id}")
    private String appId;

    @Value("${weixin.app_secret}")
    private String appSecret;

    @Value("${qq.app_id}")
    private String qqAppId;

    @Value("${qq.app_secret}")
    private String qqAppSecret;

    public static String AppId;

    public static String AppSecret;

    public static String QQAppId;

    public static String QQAppSecret;

    @Override
    public void afterPropertiesSet() throws Exception {
        AppId = appId;
        AppSecret = appSecret;
        QQAppId = qqAppId;
        QQAppSecret = qqAppSecret;
    }
}
