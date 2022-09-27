package site.haihui.challenge.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import site.haihui.challenge.common.auth.LoginRequire;
import site.haihui.challenge.common.auth.UserContext;
import site.haihui.challenge.common.exception.BadRequestException;
import site.haihui.challenge.common.response.ResponseResult;
import site.haihui.challenge.constant.CoinSource;
import site.haihui.challenge.entity.Round;
import site.haihui.challenge.service.ICoinRecordService;
import site.haihui.challenge.service.IRoundService;
import site.haihui.challenge.vo.CoinRecordListVO;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author linghaihui
 * @since 2022-09-21
 */
@RestController
@RequestMapping("/coin")
public class CoinRecordController {

    @Autowired
    private ICoinRecordService recordService;

    @Autowired
    private IRoundService roundService;

    @PostMapping("/operate")
    @LoginRequire
    public ResponseResult<Object> actionOperateCoin(@RequestParam Integer source,
            @RequestParam(value = "round_id", required = false, defaultValue = "0") Integer roundId) {
        boolean result = false;
        if (source.equals(CoinSource.RELIVE)) {
            if (roundId == 0) {
                throw new BadRequestException("参数错误");
            }
            result = recordService.operateCoin(UserContext.getCurrentUser().getId(), source, roundId, 0);
            if (result) {
                Round round = roundService.getById(roundId);
                round.setIsOver(0);
                round.setUpdateTime(new Date());
                roundService.updateById(round);
            }
        } else {
            result = recordService.operateCoin(UserContext.getCurrentUser().getId(), source, roundId, 0);
        }
        Map<String, Integer> res = new HashMap<>();
        res.put("result", result ? 1 : 0);
        return ResponseResult.success(res);
    }

    @GetMapping("/record")
    @LoginRequire
    public ResponseResult<CoinRecordListVO> actionGetCoinRecordList(@RequestParam Integer page,
            @RequestParam Integer size) {
        return ResponseResult.success(recordService.getCoinRecord(UserContext.getCurrentUser().getId(), page, size));
    }
}
