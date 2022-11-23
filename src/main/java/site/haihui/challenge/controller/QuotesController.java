package site.haihui.challenge.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import site.haihui.challenge.common.response.ResponseResult;
import site.haihui.challenge.entity.Quotes;
import site.haihui.challenge.service.IQuotesService;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author linghaihui
 * @since 2022-11-23
 */
@RestController
@RequestMapping("/quotes")
public class QuotesController {

    @Autowired
    private IQuotesService quotesService;

    @GetMapping("/one")
    public ResponseResult<Quotes> actionGetRandomeOne() {
        return ResponseResult.success(quotesService.getRandomOne());
    }
}
