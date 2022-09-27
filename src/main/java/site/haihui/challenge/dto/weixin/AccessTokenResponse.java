package site.haihui.challenge.dto.weixin;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AccessTokenResponse {

    private String errmsg;

    private Integer errcode;

    @JsonProperty(value = "access_token")
    private String accessToken;

    @JsonProperty(value = "expires_in")
    private Integer expiresIn;
}
