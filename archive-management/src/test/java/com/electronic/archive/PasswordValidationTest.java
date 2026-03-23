package com.electronic.archive;

import com.electronic.archive.entity.SysUser;
import com.electronic.archive.service.SysUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class PasswordValidationTest {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testDatabasePassword() {
        // 查询admin用户
        SysUser user = sysUserService.getByUsername("test");
        if (user == null) {
            System.out.println("未找到admin用户");
            return;
        }

        System.out.println("用户信息:");
        System.out.println("- 用户名: " + user.getUsername());
        System.out.println("- 密码哈希: " + user.getPassword());
        System.out.println("- 密码哈希长度: " + user.getPassword().length());

        // 测试常见密码
        String[] testPasswords = {"admin", "password", "123456", "admin123", "password123"};
        
        for (String password : testPasswords) {
            boolean isMatch = passwordEncoder.matches(password, user.getPassword());
            System.out.println("\n测试密码: '" + password + "'");
            System.out.println("- 匹配结果: " + isMatch);
            if (isMatch) {
                System.out.println("✅ 密码匹配成功!");
            } else {
                System.out.println("❌ 密码匹配失败");
            }
        }

        // 测试密码编码
        System.out.println("\n\n密码编码器测试:");
        String rawPassword = "test123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        System.out.println("- 原始密码: '" + rawPassword + "'");
        System.out.println("- 编码后密码: '" + encodedPassword + "'");
        System.out.println("- 编码后长度: " + encodedPassword.length());
        boolean isMatch = passwordEncoder.matches(rawPassword, encodedPassword);
        System.out.println("- 自验证结果: " + isMatch);
        System.out.println(passwordEncoder.encode("admin123"));
    }

    public static void main(String[] args) {
        // 此方法仅用于说明如何运行测试
        System.out.println("请使用JUnit运行testDatabasePassword方法");
        System.out.println("或在IDE中右键点击此类选择'Run as JUnit Test'");
    }
}