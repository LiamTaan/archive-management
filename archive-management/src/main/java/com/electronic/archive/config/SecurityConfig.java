package com.electronic.archive.config;

import com.electronic.archive.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // 启用方法级别的权限控制
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // 配置CORS
                .cors(cors -> cors
                        .configurationSource(request -> {
                            org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
                            config.setAllowedOriginPatterns(java.util.List.of("*"));
                            config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                            config.setAllowedHeaders(java.util.List.of("*"));
                            config.setAllowCredentials(false);
                            return config;
                        })
                )
                // 禁用会话管理，使用无状态认证
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置基于角色的权限控制
                .authorizeHttpRequests(auth -> auth
                        // 允许匿名访问的路径
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/auth/logout").permitAll()
                        // Swagger文档相关路径，生产环境建议关闭
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        // 静态资源
                        .requestMatchers("/static/**").permitAll()
                        // 超级管理员权限路径
                        .requestMatchers("/user/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/role/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/permission/**").hasRole("SUPER_ADMIN")
                        // 档案管理员权限路径
                        .requestMatchers("/archive/**").hasAnyRole("SUPER_ADMIN", "ARCHIVE_ADMIN")
                        .requestMatchers("/hanging/**").hasAnyRole("SUPER_ADMIN", "ARCHIVE_ADMIN")
                        .requestMatchers("/batch/**").hasAnyRole("SUPER_ADMIN", "ARCHIVE_ADMIN")
                        .requestMatchers("/approval/**").hasAnyRole("SUPER_ADMIN", "ARCHIVE_ADMIN")
                        // 业务操作员权限路径
                        .requestMatchers("/query/**").hasAnyRole("SUPER_ADMIN", "ARCHIVE_ADMIN", "BUSINESS_OPERATOR")
                        .requestMatchers("/notification/**").hasAnyRole("SUPER_ADMIN", "ARCHIVE_ADMIN", "BUSINESS_OPERATOR")
                        // 其他所有请求需要认证
                        .anyRequest().authenticated())
                // 配置认证管理器
                .authenticationManager(authenticationManager(authenticationConfiguration))
                // 配置密码编码器
                .httpBasic(httpBasic -> httpBasic.realmName("Archive Management System"))
                // 添加JWT认证过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    
    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;
}