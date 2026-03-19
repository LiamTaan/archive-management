package com.electronic.archive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.electronic.archive.entity.SysMenu;

import java.util.List;

/**
 * 系统菜单Mapper
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    
    /**
     * 根据用户ID获取菜单列表
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<SysMenu> getMenuListByUserId(Long userId);
}
