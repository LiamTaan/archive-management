package com.electronic.archive.config;

import com.electronic.archive.entity.SysUser;
import com.electronic.archive.service.SysUserRoleService;
import com.electronic.archive.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserService sysUserService;
    private final SysUserRoleService sysUserRoleService;
    private final PasswordEncoder passwordEncoder;

    // 注入必要的服务
    public CustomUserDetailsService(SysUserService sysUserService, 
                                   SysUserRoleService sysUserRoleService, 
                                   PasswordEncoder passwordEncoder) {
        this.sysUserService = sysUserService;
        this.sysUserRoleService = sysUserRoleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserService.getByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        // 从数据库查询用户的实际角色
        List<String> roleNames = sysUserRoleService.getRoleNamesByUserId(user.getUserId());
        if (roleNames == null || roleNames.isEmpty()) {
            roleNames = List.of("SUPER_ADMIN"); // 默认角色
        }

        // 关键：返回的密码必须是「加密后的密文」，且和PasswordEncoder规则一致
        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword()) // 这里必须传加密后的密文
                .roles(roleNames.toArray(new String[0])) // 使用实际角色
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(user.getStatus() == 0) // 使用用户状态
                .build();
    }
}