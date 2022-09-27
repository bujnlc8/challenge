package site.haihui.challenge.dto.weixin;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class WeixinWaterMark {

    private Integer timestamp;

    @JsonProperty("appid")
    private String appId;
}
