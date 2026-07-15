package com.outfit.config;

import com.outfit.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录或token无效\"}");
            return false;
        }
        token = token.substring(7);
        if (!jwtUtils.validateToken(token)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"token已过期，请重新登录\"}");
            return false;
        }
        request.setAttribute("userId", jwtUtils.getUserId(token));
        request.setAttribute("role", jwtUtils.getRole(token));
        return true;
    }
}
