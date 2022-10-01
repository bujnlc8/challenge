package site.haihui.challenge.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;
import site.haihui.challenge.common.constant.Config;
import site.haihui.challenge.utils.StringUtils;

/**
 * <p>
 * 
 * </p>
 *
 * @author linghaihui
 * @since 2022-09-17
 */
@Data
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String nickname;

    private String avatar;

    private Integer sex;

    private String openid;

    private Integer appType;

    private String country;

    private String province;

    private String city;

    private Date createTime;

    private Date updateTime;

    public String getShowAvatar() {
        if (StringUtils.isBlank(this.avatar)) {
            return Config.DEFAULT_AVATAR;
        }
        return this.avatar;
    }

    public String getShowNickname() {
        if (StringUtils.isBlank(this.nickname)) {
            return Config.DEFAULT_NAME;
        }
        return this.nickname;
    }
}
