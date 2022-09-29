package site.haihui.challenge.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class Config {

    public static final Integer COUNT_DOWN = 30;

    @Getter
    @AllArgsConstructor
    public enum ResponseStatus {

        SUCCESS(10000, "请求成功"), FAIL(10001, "发生错误"), UerNotLogin(11111, "请重新登录"), NODATA(11112,
                "没有更多数据可加载"), ParamErr(11115, "参数错误"), WeixinCode2SessionErr(20001,
                        "微信code2session发生异常"), WeixinEncryptDataErr(20002, "加密数据内容异常"), DataBaseErr(30001, "数据库异常");

        private final int responseCode;

        private final String description;
    }
}
