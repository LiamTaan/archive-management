package com.electronic.archive.constants;

/**
 * 档案类型枚举
 */
public enum ArchiveTypeConstants {
    /**
     * 文书档案
     */
    DOCUMENT(1, "文书档案"),
    
    /**
     * 科技档案
     */
    TECHNOLOGY(2, "科技档案"),
    
    /**
     * 会计档案
     */
    ACCOUNTING(3, "会计档案"),
    
    /**
     * 默认档案类型
     */
    DEFAULT(0, "电子档案");
    
    /**
     * 档案类型ID
     */
    private final Integer typeId;
    
    /**
     * 档案类型名称
     */
    private final String typeName;
    
    /**
     * 构造方法
     * @param typeId 档案类型ID
     * @param typeName 档案类型名称
     */
    ArchiveTypeConstants(Integer typeId, String typeName) {
        this.typeId = typeId;
        this.typeName = typeName;
    }
    
    /**
     * 根据档案类型ID获取档案类型名称
     * @param typeId 档案类型ID
     * @return 档案类型名称
     */
    public static String getTypeNameByTypeId(Integer typeId) {
        // 处理null值，直接返回默认档案类型
        if (typeId == null) {
            return DEFAULT.getTypeName();
        }
        // 遍历所有枚举值，查找匹配的档案类型
        for (ArchiveTypeConstants type : values()) {
            if (type.getTypeId().equals(typeId)) {
                return type.getTypeName();
            }
        }
        // 如果没有匹配的档案类型，返回默认档案类型
        return DEFAULT.getTypeName();
    }
    
    /**
     * 获取档案类型ID
     * @return 档案类型ID
     */
    public Integer getTypeId() {
        return typeId;
    }
    
    /**
     * 获取档案类型名称
     * @return 档案类型名称
     */
    public String getTypeName() {
        return typeName;
    }
}