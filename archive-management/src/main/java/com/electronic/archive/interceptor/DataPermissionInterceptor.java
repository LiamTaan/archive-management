package com.electronic.archive.interceptor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.electronic.archive.annotation.DataPermission;
import com.electronic.archive.entity.SysUser;
import com.electronic.archive.service.SysDeptService;
import com.electronic.archive.service.SysUserRoleService;
import com.electronic.archive.service.SysUserService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据权限拦截器
 * 用于拦截带有@DataPermission注解的方法并实现数据权限过滤
 */
@Component
public class DataPermissionInterceptor implements MethodInterceptor {
    private static final Logger log = LoggerFactory.getLogger(DataPermissionInterceptor.class);

    private final SysUserService sysUserService;
    private final SysDeptService sysDeptService;
    private final SysUserRoleService sysUserRoleService;

    public DataPermissionInterceptor(@org.springframework.context.annotation.Lazy SysUserService sysUserService, 
                                   @org.springframework.context.annotation.Lazy SysDeptService sysDeptService,
                                   @org.springframework.context.annotation.Lazy SysUserRoleService sysUserRoleService) {
        this.sysUserService = sysUserService;
        this.sysDeptService = sysDeptService;
        this.sysUserRoleService = sysUserRoleService;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        // 获取当前方法
        Method method = invocation.getMethod();
        // 检查方法是否带有DataPermission注解
        if (method.isAnnotationPresent(DataPermission.class)) {
            // 获取注解参数
            DataPermission annotation = method.getAnnotation(DataPermission.class);
            String role = annotation.role();
            String department = annotation.department();
            boolean includeCreator = annotation.includeCreator();
            String tableAlias = annotation.tableAlias();

            // 获取当前登录用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                String username = authentication.getName();
                SysUser user = sysUserService.getByUsername(username);
                if (user != null) {
                    log.info("DataPermissionInterceptor: Processing request for user {}", username);
                    // 检查用户是否为超级管理员
                    List<String> roleNames = sysUserRoleService.getRoleNamesByUserId(user.getUserId());
                    log.info("DataPermissionInterceptor: User {} has roles: {}", username, roleNames);
                    boolean isSuperAdmin = roleNames != null && roleNames.contains("SUPER_ADMIN");
                    boolean isArchiveAdmin = roleNames != null && roleNames.contains("ARCHIVE_ADMIN");
                    
                    // 超级管理员不受限制，不添加任何过滤条件
                    if (!isSuperAdmin) {
                        log.info("DataPermissionInterceptor: User {} is not SUPER_ADMIN, applying permission filters", username);
                        // 构建数据过滤条件
                        List<Long> allowedDeptIds = getAllowedDeptIds(user.getDeptId(), department);
                        
                        // 档案管理员可以查看全量档案
                        if (isArchiveAdmin) {
                            allowedDeptIds = sysDeptService.getAllDeptIds();
                        }
                        
                        log.info("DataPermissionInterceptor: User {} allowed dept IDs: {}", username, allowedDeptIds);
                        
                        // 处理方法参数，根据参数类型添加数据过滤条件
                        Object[] args = invocation.getArguments();
                        for (int i = 0; i < args.length; i++) {
                            Object arg = args[i];
                            if (arg instanceof LambdaQueryWrapper<?>) {
                                // 如果参数是LambdaQueryWrapper，添加部门和创建人过滤条件
                                @SuppressWarnings("unchecked")
                                LambdaQueryWrapper<Object> wrapper = (LambdaQueryWrapper<Object>) arg;
                                addDeptFilterToLambdaWrapper(wrapper, allowedDeptIds, tableAlias);
                                if (includeCreator) {
                                    addCreatorFilterToLambdaWrapper(wrapper, user, tableAlias);
                                }
                            } else if (arg instanceof QueryWrapper<?>) {
                                // 如果参数是QueryWrapper，添加部门和创建人过滤条件
                                @SuppressWarnings("unchecked")
                                QueryWrapper<Object> wrapper = (QueryWrapper<Object>) arg;
                                addDeptFilterToQueryWrapper(wrapper, allowedDeptIds, tableAlias);
                                if (includeCreator) {
                                    addCreatorFilterToQueryWrapper(wrapper, user, tableAlias);
                                }
                            } else if (arg instanceof com.electronic.archive.dto.ArchiveQueryDTO) {
                                // 如果参数是ArchiveQueryDTO，添加部门过滤条件
                                com.electronic.archive.dto.ArchiveQueryDTO queryDTO = (com.electronic.archive.dto.ArchiveQueryDTO) arg;
                                addDeptFilterToArchiveQueryDTO(queryDTO, allowedDeptIds);
                            } else if (arg instanceof com.electronic.archive.dto.ArchiveCombinationQueryDTO) {
                                // 如果参数是ArchiveCombinationQueryDTO，添加部门过滤条件
                                com.electronic.archive.dto.ArchiveCombinationQueryDTO queryDTO = (com.electronic.archive.dto.ArchiveCombinationQueryDTO) arg;
                                addDeptFilterToArchiveCombinationQueryDTO(queryDTO, allowedDeptIds);
                            }
                        }
                    }
                }
            }
        }
        
        // 执行原方法
        return invocation.proceed();
    }
    
    /**
     * 获取允许访问的部门ID列表
     * @param currentDeptId 当前用户部门ID
     * @param departmentPermission 部门权限类型
     * @return 允许访问的部门ID列表
     */
    private List<Long> getAllowedDeptIds(Long currentDeptId, String departmentPermission) {
        List<Long> deptIds;
        
        // 如果currentDeptId为null，返回空列表
        if (currentDeptId == null) {
            return List.of();
        }
        
        // 根据部门权限类型扩展部门ID列表
        if ("current_and_children".equals(departmentPermission)) {
            // 查询当前部门及子部门ID
            deptIds = sysDeptService.getDeptAndChildrenIds(currentDeptId);
        } else if ("all".equals(departmentPermission)) {
            // 返回所有部门ID
            deptIds = sysDeptService.getAllDeptIds();
        } else {
            // 默认只返回当前部门
            deptIds = List.of(currentDeptId);
        }
        
        return deptIds;
    }
    
    /**
     * 向LambdaQueryWrapper添加部门过滤条件
     * @param wrapper 查询条件包装器
     * @param allowedDeptIds 允许访问的部门ID列表
     * @param tableAlias 表别名
     */
    private void addDeptFilterToLambdaWrapper(LambdaQueryWrapper<Object> wrapper, List<Long> allowedDeptIds, String tableAlias) {
        if (allowedDeptIds == null || allowedDeptIds.isEmpty()) {
            return;
        }
        
        // 使用原始SQL条件添加部门过滤
        String deptCondition = buildDeptCondition(allowedDeptIds, tableAlias);
        wrapper.apply(deptCondition);
    }
    
    /**
     * 向QueryWrapper添加部门过滤条件
     * @param wrapper 查询条件包装器
     * @param allowedDeptIds 允许访问的部门ID列表
     * @param tableAlias 表别名
     */
    private void addDeptFilterToQueryWrapper(QueryWrapper<Object> wrapper, List<Long> allowedDeptIds, String tableAlias) {
        if (allowedDeptIds == null || allowedDeptIds.isEmpty()) {
            return;
        }
        
        // 动态添加部门ID过滤条件
        String deptField = StringUtils.isBlank(tableAlias) ? "dept_id" : tableAlias + ".dept_id";
        wrapper.in(deptField, allowedDeptIds);
    }
    
    /**
     * 构建部门过滤条件SQL
     * @param allowedDeptIds 允许访问的部门ID列表
     * @param tableAlias 表别名
     * @return 部门过滤条件SQL
     */
    private String buildDeptCondition(List<Long> allowedDeptIds, String tableAlias) {
        String deptField = StringUtils.isBlank(tableAlias) ? "dept_id" : tableAlias + ".dept_id";
        String deptIds = allowedDeptIds.stream().map(String::valueOf).collect(Collectors.joining(",", "(", ")"));
        return deptField + " IN " + deptIds;
    }
    
    /**
     * 向LambdaQueryWrapper添加创建人过滤条件
     * @param wrapper 查询条件包装器
     * @param user 当前用户信息
     * @param tableAlias 表别名
     */
    private void addCreatorFilterToLambdaWrapper(LambdaQueryWrapper<Object> wrapper, SysUser user, String tableAlias) {
        if (user == null) {
            return;
        }
        
        // 获取用户角色
        List<String> roleNames = sysUserRoleService.getRoleNamesByUserId(user.getUserId());
        if (roleNames == null || roleNames.isEmpty()) {
            roleNames = List.of("USER"); // 默认角色
        }
        
        // 超级管理员不受限制
        if (roleNames.contains("SUPER_ADMIN")) {
            return;
        }
        
        String creatorField = StringUtils.isBlank(tableAlias) ? "create_by" : tableAlias + ".create_by";
        String publicField = StringUtils.isBlank(tableAlias) ? "public_flag" : tableAlias + ".public_flag";
        
        // 档案管理员：全量数据（已通过部门过滤实现）
        if (roleNames.contains("ARCHIVE_ADMIN")) {
            return;
        }
        
        // 部门负责人：本部门所有数据（已通过部门过滤实现）
        if (roleNames.contains("DEPT_LEADER")) {
            return;
        }
        
        // 档案经办人：自己创建的数据
        if (roleNames.contains("ARCHIVE_OPER")) {
            wrapper.apply(creatorField + " = '" + user.getUsername() + "'");
            return;
        }
        
        // 普通用户：只能查看自己创建的数据
        wrapper.apply(creatorField + " = '" + user.getUsername() + "'");
    }
    
    /**
     * 向QueryWrapper添加创建人过滤条件
     * @param wrapper 查询条件包装器
     * @param user 当前用户信息
     * @param tableAlias 表别名
     */
    private void addCreatorFilterToQueryWrapper(QueryWrapper<Object> wrapper, SysUser user, String tableAlias) {
        if (user == null) {
            return;
        }
        
        // 获取用户角色
        List<String> roleNames = sysUserRoleService.getRoleNamesByUserId(user.getUserId());
        if (roleNames == null || roleNames.isEmpty()) {
            roleNames = List.of("USER"); // 默认角色
        }
        
        // 超级管理员不受限制
        if (roleNames.contains("SUPER_ADMIN")) {
            return;
        }
        
        String creatorField = StringUtils.isBlank(tableAlias) ? "create_by" : tableAlias + ".create_by";
        String publicField = StringUtils.isBlank(tableAlias) ? "public_flag" : tableAlias + ".public_flag";
        
        // 档案管理员：全量数据（已通过部门过滤实现）
        if (roleNames.contains("ARCHIVE_ADMIN")) {
            return;
        }
        
        // 部门负责人：本部门所有数据（已通过部门过滤实现）
        if (roleNames.contains("DEPT_LEADER")) {
            return;
        }
        
        // 档案经办人：自己创建的数据
        if (roleNames.contains("ARCHIVE_OPER")) {
            wrapper.eq(creatorField, user.getUsername());
            return;
        }
        
        // 普通用户：只能查看自己创建的数据
        wrapper.eq(creatorField, user.getUsername());
    }
    
    /**
     * 向ArchiveQueryDTO添加部门过滤条件
     * @param queryDTO 档案查询DTO
     * @param allowedDeptIds 允许访问的部门ID列表
     */
    private void addDeptFilterToArchiveQueryDTO(com.electronic.archive.dto.ArchiveQueryDTO queryDTO, List<Long> allowedDeptIds) {
        if (queryDTO == null || allowedDeptIds == null || allowedDeptIds.isEmpty()) {
            return;
        }
        
        // 直接调用setter方法设置部门ID列表
        queryDTO.setDeptIds(allowedDeptIds);
        
        // 如果只有一个部门ID，也设置单个部门ID
        if (allowedDeptIds.size() == 1) {
            queryDTO.setDeptId(allowedDeptIds.get(0));
        }
    }
    
    /**
     * 向ArchiveCombinationQueryDTO添加部门过滤条件
     * @param queryDTO 档案组合查询DTO
     * @param allowedDeptIds 允许访问的部门ID列表
     */
    private void addDeptFilterToArchiveCombinationQueryDTO(com.electronic.archive.dto.ArchiveCombinationQueryDTO queryDTO, List<Long> allowedDeptIds) {
        if (queryDTO == null || allowedDeptIds == null || allowedDeptIds.isEmpty()) {
            return;
        }
        
        // 直接调用setter方法设置部门ID列表
        queryDTO.setDeptIds(allowedDeptIds);
        
        // 如果只有一个部门ID，也设置单个部门ID
        if (allowedDeptIds.size() == 1) {
            queryDTO.setDeptId(allowedDeptIds.get(0));
        }
    }
}