package com.electronic.archive.constants;

/**
 * 角色枚举
 */
public enum RoleConstants {
    /**
     * 档案经办人
     */
    DEPT_ADMIN(4, "ARCHIVE_OPER"),
    /**
     * 部门负责人
     */
    DEPT_LEADER(3, "DEPT_LEADER"),
    /**
     * 档案管理员
     */
    ARCHIVE_ADMIN(2, "ARCHIVE_ADMIN"),
    /**
     * 档案管理员
     */
    SYSTEM_ADMIN(1, "SUPER_ADMIN");

    /**
     * 角色ID
     */
    private final Integer roleId;

    /**
     * 角色编码
     */
    private final String roleCode;

    /**
     * 构造方法
     * @param roleId 角色ID
     * @param roleCode 角色编码
     */
    RoleConstants(Integer roleId, String roleCode) {
        this.roleId = roleId;
        this.roleCode = roleCode;
    }

    /**
     * 根据角色ID获取角色名称
     * @param roleId 角色ID
     * @return 角色名称
     */
    public static String getRoleNameByRoleId(Integer roleId) {
        for (RoleConstants role : values()) {
            if (role.getRoleId().equals(roleId)) {
                return role.getRoleCode();
            }
        }
        return "未知角色";
    }

    /**
     * 获取角色ID
     * @return 角色ID
     */
    public Integer getRoleId() {
        return roleId;
    }

    /**
     * 获取角色名称
     * @return 角色名称
     */
    public String getRoleCode() {
        return roleCode;
    }

}
