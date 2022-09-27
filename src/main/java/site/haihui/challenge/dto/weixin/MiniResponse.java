package site.haihui.challenge.dto.weixin;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class MiniResponse {

    @JsonProperty(value = "errcode")
    private Integer errCode;

    @JsonProperty(value = "errmsg")
    private String errMsg;
}
