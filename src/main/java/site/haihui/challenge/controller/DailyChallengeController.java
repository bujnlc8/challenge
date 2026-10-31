package site.haihui.challenge.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.haihui.challenge.common.auth.LoginRequire;
import site.haihui.challenge.common.auth.UserContext;
import site.haihui.challenge.common.constant.AppType;
import site.haihui.challenge.common.exception.BaseException;
import site.haihui.challenge.common.response.ResponseResult;
import site.haihui.challenge.daily.DailyChallengeService;

@RestController
@RequestMapping("/daily-challenge")
public class DailyChallengeController {
    private final DailyChallengeService service;
    public DailyChallengeController(DailyChallengeService service) {
        this.service = service;
    }

    private int wechatUid() {
        if (!AppType.WXMINIAPP.getAppType().equals(UserContext.getCurrentUser().getAppType()))
            throw new BaseException(400, "请在微信小程序中设置提醒");
        return UserContext.getCurrentUser().getId();
    }

    @GetMapping("/today")
    @LoginRequire
    public ResponseResult<Map<String, Object>> today() {
        return ResponseResult.success(service.today());
    }

    @GetMapping("/reminder")
    @LoginRequire
    public ResponseResult<Map<String, Object>> reminder() {
        return ResponseResult.success(service.reminder(wechatUid()));
    }

    @PostMapping("/reminder")
    @LoginRequire
    public ResponseResult<Map<String, Object>> reminder(@RequestParam boolean enabled) {
        return ResponseResult.success(service.setReminder(wechatUid(), enabled));
    }

    @PostMapping("/reminder/accept")
    @LoginRequire
    public ResponseResult<Map<String, Object>> accept(@RequestParam("request_id") String requestId,
        @RequestParam("template_id") String templateId, @RequestParam String result) {
        return ResponseResult.success(service.accept(wechatUid(), requestId, templateId, result));
    }
}
