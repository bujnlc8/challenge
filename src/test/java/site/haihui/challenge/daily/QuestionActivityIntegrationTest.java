package site.haihui.challenge.daily;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import site.haihui.challenge.common.auth.UserContext;
import site.haihui.challenge.entity.*;
import site.haihui.challenge.mapper.*;
import site.haihui.challenge.service.*;
import site.haihui.challenge.service.impl.QuestionServiceImpl;
import site.haihui.challenge.utils.RedisLock;

class QuestionActivityIntegrationTest {
    DailyActivityTest data;
    QuestionServiceImpl core;
    Question question;
    Round round;
    RoundDetail detail;
    IShareService share;
    RoundDetailMapper details;
    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() throws Exception {
        data = new DailyActivityTest();
        data.setup();
        core = new QuestionServiceImpl();
        QuestionMapper questions = mock(QuestionMapper.class);
        RoundMapper rounds = mock(RoundMapper.class);
        details = mock(RoundDetailMapper.class);
        share = mock(IShareService.class);
        IRedisService<Object> cache = mock(IRedisService.class);
        IRedisService<Integer> integerCache = mock(IRedisService.class);
        ICoinRecordService coins = mock(ICoinRecordService.class);
        RedisLock lock = mock(RedisLock.class);
        ReflectionTestUtils.setField(core, "questionMapper", questions);
        ReflectionTestUtils.setField(core, "roundMapper", rounds);
        ReflectionTestUtils.setField(core, "roundDetailMapper", details);
        ReflectionTestUtils.setField(core, "shareService", share);
        ReflectionTestUtils.setField(core, "redisService", cache);
        ReflectionTestUtils.setField(core, "redisServiceInt", integerCache);
        ReflectionTestUtils.setField(core, "coinRecordService", coins);
        ReflectionTestUtils.setField(core, "redisLock", lock);
        ReflectionTestUtils.setField(core, "wrongQuestionBookMapper", mock(WrongQuestionBookMapper.class));
        ReflectionTestUtils.setField(core, "dailyActivityService", data.service);
        question = new Question();
        question.setId(20);
        question.setStatus(1);
        question.setAnswer(1);
        question.setOptions("甲|乙");
        round = new Round();
        round.setId(77);
        round.setUid(1);
        round.setScore(0);
        round.setIsOver(0);
        round.setCorrectQuestion(0);
        round.setTimeout(30);
        round.setTotalQuestion(1);
        detail = new RoundDetail();
        detail.setId(501);
        detail.setUid(1);
        detail.setRoundId(77);
        detail.setQuestionId(20);
        detail.setScore(0);
        detail.setCostSecond(0);
        detail.setAnswer(0);
        when(questions.selectById(20)).thenReturn(question);
        when(cache.get("challenge:question:20")).thenReturn(question);
        when(rounds.selectById(77)).thenReturn(round);
        when(details.selectOne(any())).thenReturn(detail);
        when(details.updateById(any(RoundDetail.class))).thenReturn(1);
        when(rounds.insert(any(Round.class))).thenAnswer(invocation -> {
            ((Round) invocation.getArgument(0)).setId(77);
            return 1;
        });
        when(cache.sSize(anyString())).thenReturn(1L);
        when(cache.countZSet(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(share.getCacheKey(anyInt(), anyInt())).thenReturn("daily-test");
        when(share.getUserMaxScore(anyInt(), anyInt())).thenReturn(100000);
        when(share.isQuesetionSet(1, 20, 8)).thenReturn(true);
        when(coins.getGrade(1)).thenReturn(2);
        when(lock.tryLock(anyString(), anyLong())).thenReturn("lock-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        User user = new User();
        user.setId(1);
        user.setAppType(1);
        UserContext.putCurrebtUser(user);
    }
    @AfterEach
    void cleanup() {
        RequestContextHolder.resetRequestAttributes();
    }
    long count() {
        return (Long) data.service.activity(1, 1).get("total_count");
    }
    @Test
    void rightAndWrongChallengeAnswersCountAfterAcceptedPath() {
        data.transactions.execute(status -> {
            core.checkAnswer(1, 77, 20, 25, 0, 1);
            return null;
        });
        assertEquals(1, count());
        detail.setId(502);
        detail.setScore(0);
        detail.setCostSecond(0);
        detail.setAnswer(0);
        round.setIsOver(0);
        data.transactions.execute(status -> {
            core.checkAnswer(1, 77, 20, 25, 0, 2);
            return null;
        });
        assertEquals(2, count());
    }
    @Test
    void challengeDuplicateSubmissionDoesNotDoubleCount() {
        core.checkAnswer(1, 77, 20, 25, 0, 1);
        core.checkAnswer(1, 77, 20, 25, 0, 1);
        assertEquals(1, count());
    }
    @Test
    void practiceRightWrongAndRetriesUseExistingDailyUniqueQuestionSemantics() {
        core.checkTrainingQuestion(1, 20, 2);
        core.checkTrainingQuestion(1, 20, 2);
        assertEquals(1, count());
        question.setId(21);
        when(share.isQuesetionSet(1, 21, 8)).thenReturn(true);
        // A separate issued question uses the existing cache contract.
        IRedisService<Object> cache = (IRedisService<Object>) ReflectionTestUtils.getField(core, "redisService");
        when(cache.get("challenge:question:21")).thenReturn(question);
        core.checkTrainingQuestion(1, 21, 1);
        assertEquals(2, count());
    }
    @Test
    void prelaunchAnsweredChallengeRetryIsNotInventedAsNewActivity() {
        detail.setAnswer(1);
        detail.setScore(20);
        detail.setCostSecond(5);
        core.checkAnswer(1, 77, 20, 25, 0, 1);
        assertEquals(0, count());
    }

    @Test
    void prelaunchAnsweredPracticeRetryIsNotInventedAsNewActivity() {
        when(share.isQuesetionSet(1, 20, 7)).thenReturn(true);
        core.checkTrainingQuestion(1, 20, 1);
        assertEquals(0, count());
    }

    @Test
    void priorPracticeActivityLookupFailureCannotFailAcceptedAnswer() {
        when(share.isQuesetionSet(1, 20, 7)).thenThrow(new IllegalStateException("analytics lookup unavailable"));
        assertDoesNotThrow(() -> core.checkTrainingQuestion(1, 20, 1));
        assertEquals(0, count());
    }

    @Test
    void missingPersistedUpdateCannotCreateContribution() {
        when(details.updateById(any(RoundDetail.class))).thenReturn(0);
        core.checkAnswer(1, 77, 20, 25, 0, 1);
        assertEquals(0, count());
    }

    @Test
    void viewingQuestionAndTimeoutDoNotCount() {
        core.challengeQuestion(1, 0, 20, 0);
        assertEquals(0, count());
        core.checkAnswer(1, 77, 20, 0, 1, 1);
        assertEquals(0, count());
    }
    @Test
    void failedMissingUnissuedOrInvalidAnswersDoNotCount() {
        when(details.selectOne(any())).thenReturn(null);
        core.checkAnswer(1, 77, 20, 25, 0, 1);
        assertEquals(0, count());
        when(share.isQuesetionSet(1, 20, 8)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> core.checkTrainingQuestion(1, 20, 1));
        when(share.isQuesetionSet(1, 20, 8)).thenReturn(true);
        core.checkTrainingQuestion(1, 20, 99);
        assertEquals(0, count());
    }
}
