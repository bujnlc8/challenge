package site.haihui.challenge.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import site.haihui.challenge.entity.Question;
import site.haihui.challenge.vo.CheckAnswerVO;
import site.haihui.challenge.vo.QuestionListVO;
import site.haihui.challenge.vo.RankingListVO;
import site.haihui.challenge.vo.WrongQuestionVO;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author linghaihui
 * @since 2022-09-17
 */
public interface IQuestionService extends IService<Question> {

    public QuestionListVO challengeQuestion(Integer uid, Integer roundId, Integer questionId);

    public CheckAnswerVO checkAnswer(Integer uid, Integer roundId, Integer questionId, Integer countDown,
            Integer timeout,
            Integer answer);

    public RankingListVO getRankingList(Integer type);

    public List<WrongQuestionVO> getWrongQuestionList(Integer uid, Integer page, Integer size);

    public void clearQuestionCache(Integer id);

    public boolean deleteWrongQuestion(Integer uid, Integer id);

    public void hideQuestion(Integer uid, Integer id, Integer t);

    public QuestionListVO trainingQuestions(Integer uid);

    public CheckAnswerVO checkTrainingQuestion(Integer uid, Integer questionId, Integer answer);
}
