package site.haihui.challenge.daily;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import site.haihui.challenge.common.constant.CoinSource;
import site.haihui.challenge.common.constant.Config;
import site.haihui.challenge.common.exception.BaseException;
import site.haihui.challenge.entity.Question;
import site.haihui.challenge.entity.Round;
import site.haihui.challenge.mapper.QuestionMapper;
import site.haihui.challenge.mapper.RoundDetailMapper;
import site.haihui.challenge.mapper.RoundMapper;
import site.haihui.challenge.service.ICoinRecordService;
import site.haihui.challenge.service.IRedisService;
import site.haihui.challenge.service.IShareService;
import site.haihui.challenge.service.impl.QuestionServiceImpl;
import site.haihui.challenge.vo.QuestionListVO;

class QuestionDirectEntryTest {
    QuestionServiceImpl service;
    QuestionMapper questions;
    RoundMapper rounds;
    ICoinRecordService coins;
    IRedisService<Object> cache;
    IRedisService<Integer> integerCache;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() {
        service = new QuestionServiceImpl();
        questions = mock(QuestionMapper.class);
        rounds = mock(RoundMapper.class);
        coins = mock(ICoinRecordService.class);
        cache = mock(IRedisService.class);
        integerCache = mock(IRedisService.class);
        ReflectionTestUtils.setField(service, "questionMapper", questions);
        ReflectionTestUtils.setField(service, "roundMapper", rounds);
        ReflectionTestUtils.setField(service, "roundDetailMapper", mock(RoundDetailMapper.class));
        ReflectionTestUtils.setField(service, "shareService", mock(IShareService.class));
        ReflectionTestUtils.setField(service, "coinRecordService", coins);
        ReflectionTestUtils.setField(service, "redisService", cache);
        ReflectionTestUtils.setField(service, "redisServiceInt", integerCache);
        when(coins.getGrade(1)).thenReturn(2);
        when(integerCache.get(anyString())).thenReturn(3);
        when(rounds.insert(any(Round.class))).thenAnswer(invocation -> {
            ((Round) invocation.getArgument(0)).setId(77);
            return 1;
        });
        when(questions.selectList(any())).thenReturn(Collections.singletonList(question(101, 1, "随机题目")));
    }

    @Test
    void missingExplicitQuestionDoesNotFallBackToRandomOrCreateRound() {
        BaseException error = assertThrows(BaseException.class, () -> service.challengeQuestion(1, 0, 404, 0));
        assertEquals("题目不存在或已下架", error.getMsg());
        verifyNoInteractions(rounds, coins);
        verify(questions, never()).selectList(any());
    }

    @Test
    void hiddenExplicitQuestionDoesNotFallBackToRandomOrCreateRound() {
        when(questions.selectById(20)).thenReturn(question(20, 0, "下架题目"));
        BaseException error = assertThrows(BaseException.class, () -> service.challengeQuestion(1, 0, 20, 0));
        assertEquals("题目不存在或已下架", error.getMsg());
        verifyNoInteractions(rounds, coins);
        verify(questions, never()).selectList(any());
    }

    @Test
    void staleActiveCacheCannotBypassHiddenQuestionStatus() {
        when(cache.get("challenge:question:20")).thenReturn(question(20, 1, "旧缓存内容"));
        when(questions.selectById(20)).thenReturn(question(20, 0, "下架题目"));
        BaseException error = assertThrows(BaseException.class, () -> service.challengeQuestion(1, 0, 20, 0));
        assertEquals("题目不存在或已下架", error.getMsg());
        verifyNoInteractions(rounds, coins);
    }

    @Test
    void validExplicitQuestionKeepsDatabaseQuestionIdentityAndContent() {
        when(cache.get("challenge:question:20")).thenReturn(question(20, 1, "旧缓存内容"));
        when(questions.selectById(20)).thenReturn(question(20, 1, "最新题目内容"));
        QuestionListVO result = service.challengeQuestion(1, 0, 20, 0);
        assertEquals(1, result.getQuestionNum());
        assertEquals(20, result.getList().get(0).getId());
        assertEquals("最新题目内容", result.getList().get(0).getContent());
        verify(questions, never()).selectList(any());
    }

    @Test
    void zeroQuestionIdKeepsRandomChallengeAndExistingRoundBenefits() {
        QuestionListVO result = service.challengeQuestion(1, 0, 0, 0);
        assertEquals(1, result.getQuestionNum());
        assertEquals(101, result.getList().get(0).getId());
        assertEquals(77, result.getRoundId());
        assertEquals(Config.COUNT_DOWN + 2, result.getTimeout());
        assertEquals(3, result.getLastSkipChance());
        assertEquals(3, result.getLastReliveTimes());
        verify(questions, never()).selectById(any());
        verify(coins).operateCoin(1, CoinSource.ANSWER, 77, 0);
    }

    private Question question(int id, int status, String content) {
        Question question = new Question();
        question.setId(id);
        question.setStatus(status);
        question.setContent(content);
        question.setOptions("甲|乙");
        question.setLevel(1);
        question.setCategory(2);
        return question;
    }
}
