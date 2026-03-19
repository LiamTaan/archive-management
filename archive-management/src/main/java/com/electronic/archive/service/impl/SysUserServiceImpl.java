package com.electronic.archive.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electronic.archive.entity.SysUser;
import com.electronic.archive.mapper.SysUserMapper;
import com.electronic.archive.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public SysUser getByUsername(String username) {
        return sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = getByUsername(username);
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户名不存在");
        }
        if (sysUser.getStatus() == 0) {
            throw new UsernameNotFoundException("用户已禁用");
        }

        // 这里简化处理，实际应该查询用户的角色和权限
        return User.withUsername(sysUser.getUsername())
                .password(sysUser.getPassword())
                .roles("USER") // 临时硬编码角色，后续会从数据库查询
                .build();
    }
    
    @Override
    public void resetPassword(Long userId, String newPassword) {
        SysUser sysUser = sysUserMapper.selectById(userId);
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        
        // 加密新密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        sysUser.setPassword(encodedPassword);
        sysUserMapper.updateById(sysUser);
    }
    
    @Override
    public void updatePassword(String username, String oldPassword, String newPassword) {
        SysUser sysUser = getByUsername(username);
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户名不存在");
        }
        
        // 验证旧密码是否正确
        if (!passwordEncoder.matches(oldPassword, sysUser.getPassword())) {
            throw new IllegalArgumentException("旧密码错误");
        }
        
        // 加密新密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        sysUser.setPassword(encodedPassword);
        sysUserMapper.updateById(sysUser);
    }
}