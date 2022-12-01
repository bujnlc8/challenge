package site.haihui.challenge.controller;

import java.util.Arrays;
import java.util.List;

import com.tencent.cloud.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import site.haihui.challenge.common.auth.LoginRequire;
import site.haihui.challenge.common.response.ResponseResult;
import site.haihui.challenge.service.IShareService;
import site.haihui.challenge.utils.QcloudUtil;

@RestController
@RequestMapping("/common")
public class CommonController {

    @Autowired
    private QcloudUtil qcloudUtil;

    @Autowired
    private IShareService shareService;

    /**
     * 获取腾讯云临时上传凭证
     * 
     * @return
     */
    @GetMapping("/qcloud/temporay_key")
    @LoginRequire
    public ResponseResult<Object> actionGetQcloudTemporaryKey() {
        Response response = qcloudUtil.getTemporaryKey();
        if (null == response) {
            return ResponseResult.fail("获取临时上传凭证失败");
        }
        return ResponseResult.success(response);
    }

    @GetMapping("/captcha")
    @LoginRequire
    public ResponseResult<Object> actionGetCaptcha() {
        return ResponseResult.success(shareService.getCaptcha());
    }

    @PostMapping("/feedback")
    @LoginRequire
    public ResponseResult<Object> actionCheckCaptch(@RequestParam String token, @RequestParam String captcha,
            @RequestParam Integer type, @RequestParam String remark,
            @RequestParam(value = "question_id") Integer questionId) {
        return ResponseResult.success(shareService.feedback(questionId, token, captcha, type, remark));
    }

    @GetMapping("/category")
    public ResponseResult<List<String>> actionGetCategory() {
        return ResponseResult
                .success(Arrays.asList("不限", "财经", "百科", "历史", "地理", "诗词", "驾考科一", "驾考科三理论", "交通规则", "基金从业真题", "⚽️世界杯"));
    }
}
