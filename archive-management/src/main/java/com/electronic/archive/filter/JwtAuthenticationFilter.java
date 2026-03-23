package com.electronic.archive.filter;

import com.electronic.archive.config.CustomUserDetailsService;
import com.electronic.archive.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService customUserDetailsService) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. 获取请求路径，跳过登录接口（双重保障）
        String requestURI = request.getRequestURI();
        if ("/archive/auth/login".equals(requestURI)) {
            filterChain.doFilter(request, response);
            return; // 直接放行，不执行后续逻辑
        }


        // 从请求头中获取Token
        String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String token = null;

        // 解析Token
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            username = jwtUtil.getUsernameFromToken(token);
        }

        // 如果Token有效且用户未被认证
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 验证Token
            if (jwtUtil.validateToken(token)) {
                // 加载用户信息
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                // 创建认证Token
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                // 设置认证详情
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // 将认证Token存入SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }
}