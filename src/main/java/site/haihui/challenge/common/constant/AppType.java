package site.haihui.challenge.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AppType {

    WXMINIAPP(1, "微信小程序"), QQMINIAPP(2, "QQ小程序"), ALIPAY(3, "支付宝小程序");

    private final Integer appType;

    private final String remark;

    public static AppType getAppType(Integer app_type) {
        if (app_type == 1) {
            return AppType.WXMINIAPP;
        } else if (app_type == 2) {
            return AppType.QQMINIAPP;
        }
        return null;
    }
}
