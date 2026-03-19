package com.electronic.archive.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;
import java.util.List;

/**
 * 适配 MyBatis-Plus 的通用分页响应
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> {
    /**
     * 当前页码
     */
    private Long pageNum;

    /**
     * 每页条数
     */
    private Long pageSize;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Long totalPages;

    /**
     * 当前页数据列表
     */
    private List<T> list;

    /**
     * 为了兼容 MyBatis Plus 的 IPage 格式，添加 records 字段的 getter/setter
     */
    public List<T> getRecords() {
        return this.list;
    }

    public void setRecords(List<T> records) {
        this.list = records;
    }

    /**
     * 从 MyBatis-Plus 的 IPage 转换为通用分页结果（核心方法）
     * @param iPage MyBatis-Plus 分页对象
     * @param <T> 数据类型
     * @return 通用分页结果
     */
    public static <T> PageResult<T> fromMpPage(IPage<T> iPage) {
        PageResult<T> result = new PageResult<>();
        result.setPageNum(iPage.getCurrent());
        result.setPageSize(iPage.getSize());
        result.setTotal(iPage.getTotal());
        result.setTotalPages(iPage.getPages());
        result.setList(iPage.getRecords());
        return result;
    }

    /**
     * 空分页结果（便捷方法）
     * @param pageRequest 分页请求
     * @param <T> 数据类型
     * @return 空结果
     */
    public static <T> PageResult<T> empty(PageRequest pageRequest) {
        pageRequest.validate();
        PageResult<T> result = new PageResult<>();
        result.setPageNum(pageRequest.getPageNum());
        result.setPageSize(pageRequest.getPageSize());
        result.setTotal(0L);
        result.setTotalPages(0L);
        result.setList(List.of());
        return result;
    }
}