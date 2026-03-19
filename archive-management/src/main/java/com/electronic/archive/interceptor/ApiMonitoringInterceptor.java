package com.electronic.archive.interceptor;

import com.electronic.archive.service.SystemLogService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * API监控拦截器
 * 用于记录接口调用情况并生成监控指标
 */
@Component
public class ApiMonitoringInterceptor implements HandlerInterceptor {

    private final MeterRegistry meterRegistry;
    private final ThreadLocal<Long> startTimeThreadLocal = new ThreadLocal<>();
    private final SystemLogService systemLogService;

    @Autowired
    public ApiMonitoringInterceptor(MeterRegistry meterRegistry, SystemLogService systemLogService) {
        this.meterRegistry = meterRegistry;
        this.systemLogService = systemLogService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 记录请求开始时间
        startTimeThreadLocal.set(System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 计算请求处理时间
        long startTime = startTimeThreadLocal.get();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        startTimeThreadLocal.remove();

        String requestUri = request.getRequestURI();
        String httpMethod = request.getMethod();
        int statusCode = response.getStatus();

        // 记录请求次数指标
        Counter.builder("api.request.count")
                .tag("uri", requestUri)
                .tag("method", httpMethod)
                .tag("status", String.valueOf(statusCode))
                .description("API请求次数统计")
                .register(meterRegistry)
                .increment();

        // 记录请求处理时间指标
        Timer.builder("api.request.duration")
                .tag("uri", requestUri)
                .tag("method", httpMethod)
                .tag("status", String.valueOf(statusCode))
                .description("API请求处理时间统计")
                .register(meterRegistry)
                .record(duration, TimeUnit.MILLISECONDS);

        // 记录异常次数指标
        if (ex != null) {
            Counter.builder("api.request.exception.count")
                    .tag("uri", requestUri)
                    .tag("method", httpMethod)
                    .tag("exception", ex.getClass().getSimpleName())
                    .description("API请求异常次数统计")
                    .register(meterRegistry)
                    .increment();
        }
        
        // 获取当前登录用户
        String operateBy = "anonymous";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            operateBy = authentication.getName();
        }
        
        // 获取客户端IP
        String operateIp = request.getRemoteAddr();
        if (request.getHeader("X-Forwarded-For") != null) {
            operateIp = request.getHeader("X-Forwarded-For");
        }
        
        // 保存系统日志
        if (ex != null) {
            // 异常日志
            systemLogService.saveExceptionLog(
                    "API请求异常",
                    String.format("请求URI: %s, 请求方法: %s, 状态码: %d, 处理时间: %dms", requestUri, httpMethod, statusCode, duration),
                    operateBy,
                    operateIp,
                    ex
            );
        } else {
            // 正常日志
            systemLogService.saveSystemLog(
                    0, // INFO级别
                    4, // 性能监控类型
                    "API请求",
                    String.format("请求URI: %s, 请求方法: %s, 状态码: %d, 处理时间: %dms", requestUri, httpMethod, statusCode, duration),
                    operateBy,
                    operateIp
            );
        }
    }
}