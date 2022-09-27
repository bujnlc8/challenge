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

    public static String AppId;

    public static String AppSecret;

    @Override
    public void afterPropertiesSet() throws Exception {
        AppId = appId;
        AppSecret = appSecret;
    }
}
