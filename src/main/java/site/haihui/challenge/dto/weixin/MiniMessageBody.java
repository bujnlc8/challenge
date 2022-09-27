package site.haihui.challenge.dto.weixin;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 微信小程序订阅消息请求体
 * MiniMessageBody
 * 
 * @author: linghaihui
 */
@Data
public class MiniMessageBody {

    private String touser;

    @JsonProperty(value = "template_id")
    private String templateId;

    private String page = "";

    private Map<String, MiniTemplateData> data;

    @JsonProperty(value = "miniprogram_state")
    private String miniprogramState = "";
}
