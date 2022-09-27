package site.haihui.challenge.dto.weixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 *
 * WeixinCode2Session
 * 
 * @author: linghaihui
 */
@Data
public class WeixinCode2Session {

    private Integer errcode;

    private String errmsg;

    @JsonProperty("openid")
    private String openId;

    @JsonProperty("unionid")
    private String unionId;

    @JsonProperty("session_key")
    private String sessionKey;
}
