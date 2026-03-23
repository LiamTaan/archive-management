package com.electronic.archive.controller;

import com.electronic.archive.entity.SysDept;
import com.electronic.archive.entity.SysUser;
import com.electronic.archive.service.SysDeptService;
import com.electronic.archive.service.SysUserRoleService;
import com.electronic.archive.service.SysUserService;
import com.electronic.archive.util.JwtUtil;
import com.electronic.archive.vo.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysDeptService sysDeptService;

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ResponseResult<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty() ||
                loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            return ResponseResult.fail("用户名和密码不能为空");
        }
        try {
            // 使用AuthenticationManager进行认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername().trim(),
                            loginRequest.getPassword().trim()
                    )
            );
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseResult.fail("用户名或密码错误");
            }
            // 将认证信息存入SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 查找用户
            SysUser user = sysUserService.getByUsername(loginRequest.getUsername());
            if (user == null) {
                return ResponseResult.fail("用户名或密码错误");
            }

            // 获取用户角色列表
            List<String> roleNames = sysUserRoleService.getRoleNamesByUserId(user.getUserId());
            if (roleNames == null || roleNames.isEmpty()) {
                roleNames = List.of("SUPER_ADMIN"); // 默认角色
            }

            // 获取用户部门信息
            SysDept dept = null;
            String deptCode = "";
            String deptTreePath = "";
            if (user.getDeptId() != null) {
                dept = sysDeptService.getById(user.getDeptId());
                if (dept != null) {
                    deptCode = dept.getDeptCode();
                    deptTreePath = dept.getTreePath();
                }
            }

            // 生成JWT Token
            String token = jwtUtil.generateToken(loginRequest.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", Map.of(
                    "userId", user.getUserId(),
                    "username", user.getUsername(),
                    "nickname", user.getNickname(),
                    "email", user.getEmail(),
                    "phone", user.getPhone(),
                    "deptId", user.getDeptId(),
                    "deptName", dept != null ? dept.getDeptName() : "",
                    "deptCode", deptCode,
                    "deptTreePath", deptTreePath,
                    "roles", roleNames
            ));

            return ResponseResult.success("登录成功", response);
        } catch (BadCredentialsException e) {
            // 登录失败，可能是密码错误或其他认证问题
            return ResponseResult.fail("用户名或密码错误");
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常堆栈信息，便于调试
            return ResponseResult.fail("登录失败: " + e.getMessage());
        }
    }

    @Operation(summary = "用户注销")
    @PostMapping("/logout")
    public ResponseResult<Void> logout() {
        // 清除SecurityContext
        SecurityContextHolder.clearContext();
        return ResponseResult.success("注销成功");
    }

    /**
     * 修改用户密码
     * @param request 请求参数，包含旧密码和新密码
     * @return 操作结果
     */
    @Operation(summary = "修改用户密码")
    @PutMapping("/update-password")
    public ResponseResult<Void> updatePassword(@RequestBody Map<String, String> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return ResponseResult.fail("用户未登录");
        }
        
        String username = authentication.getName();
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        
        sysUserService.updatePassword(username, oldPassword, newPassword);
        return ResponseResult.success("修改密码成功");
    }

    // 登录请求参数类
    public static class LoginRequest {
        private String username;
        private String password;

        // getter and setter
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}