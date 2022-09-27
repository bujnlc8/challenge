package site.haihui.challenge.dto;

import lombok.Data;

/**
 * 微信登录请求参数
 * WeixinLoginDTO
 * 
 * @author: linghaihui
 */
@Data
public class WeixinLoginDTO extends BaseDTO {

    /**
     * 通过 wx.login 接口获得的临时登录凭证 code
     */
    private String code;

    /**
     * wx.getUserInfo 接口返回iv字段
     */
    private String iv;

    /**
     * wx.getUserInfo 接口返回encryptedData 字段
     */
    private String encryptedData;
}
