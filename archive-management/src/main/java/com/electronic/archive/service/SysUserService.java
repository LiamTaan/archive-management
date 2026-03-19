package com.electronic.archive.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.electronic.archive.entity.SysUser;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface SysUserService extends IService<SysUser>, UserDetailsService {
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    SysUser getByUsername(String username);
    
    /**
     * 重置用户密码
     * @param userId 用户ID
     * @param newPassword 新密码
     */
    void resetPassword(Long userId, String newPassword);
    
    /**
     * 修改用户密码
     * @param username 用户名
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void updatePassword(String username, String oldPassword, String newPassword);
}