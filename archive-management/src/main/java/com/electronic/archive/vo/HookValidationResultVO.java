package com.electronic.archive.vo;

import lombok.Data;

import java.util.List;

/**
 * 挂接校验结果VO
 * 用于返回挂接校验结果
 */
@Data
public class HookValidationResultVO {
    /**
     * 整体校验结果
     */
    private boolean valid;
    
    /**
     * 校验项列表
     */
    private List<ValidationItem> items;
    
    /**
     * 校验建议
     */
    private String suggestion;
    
    /**
     * 校验项
     */
    @Data
    public static class ValidationItem {
        /**
         * 校验项名称
         */
        private String name;
        
        /**
         * 校验结果
         */
        private boolean passed;
        
        /**
         * 错误信息
         */
        private String errorMsg;
        
        /**
         * 校验类型
         */
        private String type;
    }
}