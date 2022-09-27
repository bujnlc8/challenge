package site.haihui.challenge.common.auth;

import javax.servlet.http.HttpSession;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import site.haihui.challenge.entity.User;

/**
 * 保存和获取当前用户的工具类
 */
public class UserContext {

    private static final String CURRENT_USER_IN_SESSION = "currentUser";

    /**
     * 得到session
     */
    private static HttpSession getSession() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getSession();
    }

    /**
     * 设置当前用户到session中
     */
    public static void putCurrebtUser(User currentUser) {
        getSession().setAttribute(CURRENT_USER_IN_SESSION, currentUser);
    }

    /**
     * 获取当前用户
     */
    public static User getCurrentUser() {
        return (User) getSession().getAttribute(CURRENT_USER_IN_SESSION);
    }
}
