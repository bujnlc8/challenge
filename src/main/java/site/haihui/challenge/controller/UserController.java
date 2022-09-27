package site.haihui.challenge.controller;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import site.haihui.challenge.common.auth.LoginRequire;
import site.haihui.challenge.common.auth.UserContext;
import site.haihui.challenge.common.response.ResponseResult;
import site.haihui.challenge.dto.WeixinLoginDTO;
import site.haihui.challenge.service.IUserService;
import site.haihui.challenge.vo.MineVO;
import site.haihui.challenge.vo.WeixinLoginResponseVO;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author linghaihui
 * @since 2022-09-17
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private IUserService userService;

    @PostMapping("/login-by-weixin")
    public ResponseResult<WeixinLoginResponseVO> actionLoginByWeixin(@RequestBody WeixinLoginDTO weixinLoginDTO)
            throws JsonProcessingException {
        log.info("Receive data: {}", weixinLoginDTO);
        return ResponseResult.success(userService.loginByWeixin(weixinLoginDTO));
    }

    @GetMapping("/mine")
    @LoginRequire
    public ResponseResult<MineVO> actionMine() {
        return ResponseResult.success(userService.getMine(UserContext.getCurrentUser().getId()));
    }

    @PostMapping("/update-nickname-avatar")
    @LoginRequire
    public ResponseResult<Object> actionUpdateNicknameAvatar(
            @RequestParam(defaultValue = "", required = false) String avatar,
            @RequestParam(defaultValue = "", required = false) String nickname) {
        userService.updateAvatarNickname(UserContext.getCurrentUser().getId(), avatar, nickname);
        return ResponseResult.success(null);
    }
}
