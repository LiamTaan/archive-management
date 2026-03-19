package com.electronic.archive.util;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

/**
 * 适配 MyBatis-Plus 的通用分页请求
 */
@Data
public class PageRequest {
    /**
     * 当前页码（默认第1页，MyBatis-Plus 页码从1开始）
     */
    private Long pageNum = 1L;

    /**
     * 每页显示条数（默认10条，限制最大100条）
     */
    private Long pageSize = 10L;

    /**
     * 排序字段（如 create_time）
     */
    private String sortField;

    /**
     * 排序方向（asc/desc，默认asc）
     */
    private String sortDirection = "asc";

    /**
     * 为前端兼容性，添加currentPage的setter，映射到pageNum
     */
    public void setCurrentPage(Long currentPage) {
        this.pageNum = currentPage;
    }

    /**
     * 为前端兼容性，添加currentPage的getter
     */
    public Long getCurrentPage() {
        return this.pageNum;
    }

    /**
     * 校验参数合法性
     */
    public void validate() {
        if (pageNum == null || pageNum < 1) {
            pageNum = 1L;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 10L;
        }
        if (sortDirection == null || (!"asc".equals(sortDirection) && !"desc".equals(sortDirection))) {
            sortDirection = "asc";
        }
    }

    /**
     * 转换为 MyBatis-Plus 的 Page 对象（核心方法）
     * @param <T> 数据类型
     * @return Page<T>
     */
    public <T> Page<T> toMpPage() {
        this.validate();
        // 创建 MyBatis-Plus 分页对象
        Page<T> page = new Page<>(pageNum, pageSize);
        // 拼接排序条件（如果有排序字段）
        if (sortField != null && !sortField.trim().isEmpty()) {
            // 防SQL注入：过滤非法字符
            String safeSortField = sortField.replaceAll("[^a-zA-Z0-9_]", "");
            if ("desc".equals(sortDirection)) {
                page.addOrder(OrderItem.desc(safeSortField));
            } else {
                page.addOrder(OrderItem.asc(safeSortField));
            }
        }
        return page;
    }
}