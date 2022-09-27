package site.haihui.challenge.vo;

import java.io.Serializable;

import lombok.Data;

/**
 * 微信登录返回对象
 * WeixinLoginResponseVO
 * 
 * @author: linghaihui
 */
@Data
public class WeixinLoginResponseVO implements Serializable {

    /**
     * 用户id
     */
    private Integer uid;

    /**
     * 用户token
     */
    private String token;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;
}
