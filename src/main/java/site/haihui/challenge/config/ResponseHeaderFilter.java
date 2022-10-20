package site.haihui.challenge.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@javax.servlet.annotation.WebFilter(urlPatterns = { "/*" })
@Component
public class ResponseHeaderFilter implements javax.servlet.Filter {

    @Autowired
    private Environment env;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse res = (HttpServletResponse) response;
        res.setHeader("X-Server", env.getProperty("backendName", "roselle.backend.challenge0"));
        chain.doFilter(request, response);
    }
}
