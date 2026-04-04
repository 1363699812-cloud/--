package com.wms.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.backend.annotation.RequireRole;
import com.wms.backend.common.Result;
import com.wms.backend.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 放行OPTIONS预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 非Controller方法直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 获取Token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证Token
        if (token == null || token.isEmpty() || JwtUtils.isTokenExpired(token)) {
            sendError(response, 401, "未登录或登录已过期");
            return false;
        }

        try {
            // 解析用户信息并存入request
            Long userId = JwtUtils.getUserId(token);
            String username = JwtUtils.getUsername(token);
            String role = JwtUtils.getRole(token);

            request.setAttribute("currentUserId", userId);
            request.setAttribute("currentUsername", username);
            request.setAttribute("currentRole", role);

            // 检查角色权限
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
            if (requireRole == null) {
                requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
            }

            if (requireRole != null) {
                String[] allowedRoles = requireRole.value();
                if (!Arrays.asList(allowedRoles).contains(role)) {
                    sendError(response, 403, "权限不足");
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            sendError(response, 401, "Token无效");
            return false;
        }
    }

    private void sendError(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        Result<?> result = Result.error(code, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
