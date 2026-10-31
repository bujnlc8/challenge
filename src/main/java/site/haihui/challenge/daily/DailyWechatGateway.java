package site.haihui.challenge.daily;

import org.springframework.stereotype.Component;
import site.haihui.challenge.common.constant.AppType;
import site.haihui.challenge.config.WeixinMiniAppConfig;
import site.haihui.challenge.dto.weixin.MiniMessageBody;
import site.haihui.challenge.dto.weixin.MiniResponse;
import site.haihui.challenge.utils.WeixinProxy;

@Component
public class DailyWechatGateway {
    private final WeixinProxy proxy;
    public DailyWechatGateway(WeixinProxy proxy) {
        this.proxy = proxy;
    }

    private String accessToken() {
        String token =
            proxy.getAccessToken(WeixinMiniAppConfig.AppId, WeixinMiniAppConfig.AppSecret, AppType.WXMINIAPP);
        if (token == null || token.isEmpty())
            throw new IllegalStateException("WeChat access token unavailable");
        return token;
    }

    public MiniResponse send(MiniMessageBody body) {
        String token;
        try {
            token = accessToken();
        } catch (RuntimeException e) {
            // The subscribe-send request has not started, so its credit can be released safely.
            throw new SendNotStartedException();
        }
        return proxy.sendMiniMessage(token, body);
    }

    public static final class SendNotStartedException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        SendNotStartedException() {
            super("WeChat message send was not started");
        }
    }

    public byte[] appShareCode(String environment) {
        return proxy.generateAppShareCode(environment, accessToken());
    }

    public byte[] shareCode(int questionId, String environment) {
        return proxy.generateQuestionShareCode(questionId, environment, accessToken());
    }
}
