package site.haihui.challenge.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import site.haihui.challenge.common.auth.LoginRequire;
import site.haihui.challenge.common.auth.UserContext;
import site.haihui.challenge.common.response.ResponseResult;
import site.haihui.challenge.service.IQuestionService;
import site.haihui.challenge.utils.StringUtils;
import site.haihui.challenge.vo.CheckAnswerVO;
import site.haihui.challenge.vo.QuestionListVO;
import site.haihui.challenge.vo.RankingListVO;
import site.haihui.challenge.vo.WrongQuestionVO;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author linghaihui
 * @since 2022-09-17
 */
@RestController
@RequestMapping("/question")
public class QuestionController {

    @Autowired
    private IQuestionService questionService;

    @PostMapping("/start-challenge")
    @LoginRequire
    public ResponseResult<QuestionListVO> actionStartChallenge(
            @RequestParam(defaultValue = "0", required = false, value = "round_id") Integer roundId,
            @RequestParam(defaultValue = "0", required = false) Integer category,
            @RequestParam(defaultValue = "0", required = false, value = "question_id") Integer questionId) {
        return ResponseResult
                .success(questionService.challengeQuestion(UserContext.getCurrentUser().getId(), roundId, questionId,
                        category));
    }

    @PostMapping("/check-answer")
    @LoginRequire
    public ResponseResult<CheckAnswerVO> actionCheckAnswer(
            @RequestParam(value = "round_id") Integer roundId,
            @RequestParam(value = "question_id") Integer questionId,
            @RequestParam(value = "count_down") Integer countDown,
            @RequestParam(value = "timeout") Integer timeOut,
            @RequestParam(value = "answer") Integer answer) {
        return ResponseResult.success(
                questionService.checkAnswer(UserContext.getCurrentUser().getId(), roundId, questionId, countDown,
                        timeOut, answer));
    }

    @GetMapping("/ranking-list")
    public ResponseResult<RankingListVO> actionRankinglist(
            @RequestParam(defaultValue = "0", required = false) Integer type) {
        return ResponseResult.success(questionService.getRankingList(type));
    }

    @GetMapping("/wrong-question-list")
    @LoginRequire
    public ResponseResult<List<WrongQuestionVO>> actionGetWrongQuestionList(
            @RequestParam(defaultValue = "1", required = false) Integer page,
            @RequestParam(defaultValue = "20", required = false) Integer size) {
        return ResponseResult
                .success(questionService.getWrongQuestionList(UserContext.getCurrentUser().getId(), page, size));
    }

    @GetMapping("/clear-question-cache")
    public ResponseResult<Object> actionClearQuestionCache(@RequestParam String sign, @RequestParam Integer id) {
        if (!StringUtils.getMd5Str(String.format("q:%s", id)).equals(sign)) {
            return ResponseResult.fail("签名错误");
        }
        questionService.clearQuestionCache(id);
        return ResponseResult.success(null);
    }

    @DeleteMapping("/delete-wrong-question")
    @LoginRequire
    public ResponseResult<Object> actionDeleteWrongQuestion(
            @RequestParam Integer id) {
        boolean delete = questionService.deleteWrongQuestion(UserContext.getCurrentUser().getId(), id);
        Map<String, Boolean> res = new HashMap<>();
        res.put("result", delete);
        return ResponseResult.success(res);
    }

    @PostMapping("/delete-wrong-question")
    @LoginRequire
    public ResponseResult<Object> actionDeleteWrongQuestionA(
            @RequestParam Integer id) {
        boolean delete = questionService.deleteWrongQuestion(UserContext.getCurrentUser().getId(), id);
        Map<String, Boolean> res = new HashMap<>();
        res.put("result", delete);
        return ResponseResult.success(res);
    }

    @PostMapping("/hide")
    @LoginRequire
    public ResponseResult<Object> actionHideQuestion(
            @RequestParam Integer id,
            @RequestParam Integer t) {
        questionService.hideQuestion(UserContext.getCurrentUser().getId(), id, t);
        return ResponseResult.success(null);
    }

    @PostMapping("/training-question")
    @LoginRequire
    public ResponseResult<QuestionListVO> actionTrainingQuestion(
            @RequestParam(defaultValue = "0", required = false) Integer category) {
        return ResponseResult
                .success(questionService.trainingQuestions(UserContext.getCurrentUser().getId(), category));
    }

    @PostMapping("/check-training-question")
    @LoginRequire
    public ResponseResult<CheckAnswerVO> actionCheckTrainingQuestion(
            @RequestParam(value = "question_id") Integer questionId,
            @RequestParam(value = "answer") Integer answer) {
        return ResponseResult
                .success(questionService.checkTrainingQuestion(UserContext.getCurrentUser().getId(), questionId,
                        answer));
    }
}
