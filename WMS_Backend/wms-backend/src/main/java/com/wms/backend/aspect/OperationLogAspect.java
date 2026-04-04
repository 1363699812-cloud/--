package com.wms.backend.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.backend.annotation.Log;
import com.wms.backend.entity.OperationLog;
import com.wms.backend.service.IOperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private IOperationLogService operationLogService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("@annotation(com.wms.backend.annotation.Log)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Object result = point.proceed();

        try {
            saveLog(point);
        } catch (Exception e) {
            // 日志记录失败不影响业务
        }

        return result;
    }

    private void saveLog(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Log logAnnotation = method.getAnnotation(Log.class);

        OperationLog operationLog = new OperationLog();
        operationLog.setModule(logAnnotation.module());
        operationLog.setOperation(logAnnotation.value());
        operationLog.setMethod(point.getTarget().getClass().getName() + "." + method.getName());

        // 记录参数
        try {
            Object[] args = point.getArgs();
            if (args != null && args.length > 0) {
                String params = objectMapper.writeValueAsString(args);
                operationLog.setParams(params.length() > 2000 ? params.substring(0, 2000) : params);
            }
        } catch (Exception ignored) {
        }

        // 从请求中获取用户信息和IP
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            Object userId = request.getAttribute("currentUserId");
            Object username = request.getAttribute("currentUsername");
            if (userId != null) {
                operationLog.setUserId(Long.valueOf(userId.toString()));
            }
            if (username != null) {
                operationLog.setUsername(username.toString());
            }
            operationLog.setIp(getIpAddress(request));
        }

        operationLogService.save(operationLog);
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
