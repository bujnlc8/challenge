package site.haihui.challenge.common.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import site.haihui.challenge.common.exception.UnAuthorizedException;
import site.haihui.challenge.entity.User;
import site.haihui.challenge.service.IRedisService;
import site.haihui.challenge.service.IShareService;

public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private IRedisService<String> redisService;

    @Autowired
    private IShareService shareService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        boolean checkLogin = null != getLoginRequire(handler);
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            if (checkLogin)
                throw new UnAuthorizedException("认证信息不存在");
        } else {
            String[] header = auth.substring(7).split("_");
            if (header.length != 2) {
                throw new UnAuthorizedException("用户不存在");
            }
            String uid = header[0];
            String token = header[1];
            try {
                String cachedToken = redisService.get(String.format("challenge:%s:token", uid));
                if (cachedToken == null || !cachedToken.equals(token)) {
                    if (checkLogin) {
                        throw new UnAuthorizedException("用户不存在");
                    }
                } else {
                    User user = shareService.getUser(Integer.parseInt(uid));
                    if (user == null && checkLogin) {
                        throw new UnAuthorizedException("用户不存在");
                    }
                    UserContext.putCurrebtUser(user);
                }
            } catch (IllegalArgumentException e) {
                if (checkLogin)
                    throw new UnAuthorizedException("认证信息格式错误");
            }
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    private LoginRequire getLoginRequire(Object handler) {
        if (handler instanceof HandlerMethod) {
            HandlerMethod method = (HandlerMethod) handler;
            return method.getMethodAnnotation(LoginRequire.class);
        }
        return null;
    }
}
