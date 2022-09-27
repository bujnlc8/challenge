package site.haihui.challenge.dto.weixin;

import lombok.Data;

/**
 * 
 * WeixinDecryptData
 * 
 * @author: linghaihui
 */
@Data
public class WeixinDecryptData {

    private String openId;

    private String nickName;

    private Integer gender;

    private String language;

    private String city;

    private String province;

    private String country;

    private String avatarUrl;

    private String unionId;

    private WeixinWaterMark watermark;
}
