package site.haihui.challenge.service;

import site.haihui.challenge.entity.User;
import site.haihui.challenge.vo.CaptchaVO;

public interface IShareService {

    public Integer getUserMaxScore(Integer uid);

    public void setUserMaxScore(Integer uid, Integer score);

    public Integer getCachedQuestionNum(Integer uid, Integer type);

    public void incrCachedQuestionNum(Integer uid, Integer type);

    public void setCachedQuestionNum(Integer uid, Integer num, Integer type);

    public User getUser(Integer uid);

    public void setCachedUser(Integer uid, User user);

    public void putQuestionSet(Integer uid, Integer questionId, Integer type);

    public boolean isQuesetionSet(Integer uid, Integer questionId, Integer type);

    public Integer getLastSkipChance(Integer uid);

    public void decreaseLastSkipChance(Integer uid);

    public Integer getWrongQuestion(Integer uid, Integer status);

    public void clearCache(String key);

    public String getCacheKey(Integer uid, Integer type);

    public CaptchaVO getCaptcha();

    public boolean feedback(Integer questionId, String token, String captcha, Integer type, String remark);
}
