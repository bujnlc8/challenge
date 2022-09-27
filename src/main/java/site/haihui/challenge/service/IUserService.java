package site.haihui.challenge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;

import site.haihui.challenge.dto.WeixinLoginDTO;
import site.haihui.challenge.entity.User;
import site.haihui.challenge.vo.MineVO;
import site.haihui.challenge.vo.WeixinLoginResponseVO;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author linghaihui
 * @since 2022-09-17
 */
public interface IUserService extends IService<User> {

    public WeixinLoginResponseVO loginByWeixin(WeixinLoginDTO data) throws JsonProcessingException;

    public MineVO getMine(Integer uid);

    public void updateAvatarNickname(Integer uid, String avatar, String nickname);
}
