package com.electronic.archive.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限注解
 * 用于标记需要进行数据权限控制的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataPermission {
    /**
     * 角色权限类型
     * 支持："ALL"（所有数据）, "OWN"（自己的数据）, "DEPT"（本部门数据）, "DEPT_AND_CHILD"（本部门及子部门数据）
     */
    String role() default "DEPT";
    
    /**
     * 部门权限类型
     * 支持："current"（当前部门）, "all"（所有部门）, "current_and_children"（当前部门及子部门）
     */
    String department() default "current";
    
    /**
     * 是否包含创建人权限过滤
     */
    boolean includeCreator() default true;
    
    /**
     * 数据权限过滤的表别名
     */
    String tableAlias() default "";
}