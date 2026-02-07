package com.police.kb.common;

/**
 * 归属地枚举
 * <p>
 * 定义文档归属地的行政级别，包括国家级、部级、省级、市级、县级和单位级。
 * 用于规范归属地字段的取值范围，提供类型安全和代码可读性。
 * </p>
 *
 * @author 黄俊
 * @date 2026-01-31 10:00:00
 */
public enum OriginScope {

    /**
     * 国家级
     */
    national("national", "国家级"),

    /**
     * 部级
     */
    ministerial("ministerial", "部级"),

    /**
     * 省级
     */
    provincial("provincial", "省级"),

    /**
     * 市级
     */
    municipal("municipal", "市级"),

    /**
     * 县级
     */
    county("county", "县级"),

    /**
     * 单位级
     */
    unit("unit", "单位级");

    /**
     * 编码
     */
    private final String code;

    /**
     * 描述
     */
    private final String description;

    /**
     * 构造函数
     *
     * @param code        编码
     * @param description 描述
     */
    OriginScope(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 获取编码
     *
     * @return 编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取描述
     *
     * @return 描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据编码获取枚举值
     *
     * @param code 编码
     * @return 枚举值，如果未找到返回null
     */
    public static OriginScope fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (OriginScope scope : values()) {
            if (scope.code.equals(code)) {
                return scope;
            }
        }
        return null;
    }

    /**
     * 判断编码是否有效
     *
     * @param code 编码
     * @return 是否有效
     */
    public static boolean isValidCode(String code) {
        return fromCode(code) != null;
    }

    @Override
    public String toString() {
        return String.format("OriginScope{%s=%s}", code, description);
    }
}
