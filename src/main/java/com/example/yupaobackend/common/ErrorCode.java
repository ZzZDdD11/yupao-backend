package com.example.yupaobackend.common;

/**
 * 全局错误码
 */
public enum ErrorCode {

    PARAMS_ERROR(40000,"请求参数错误",""),
    NULL_ERROR(40001,"请求数据为空",""),
    NO_AUTH(40101,"无权限",""),
    NOT_LOGIN(40100,"未登录",""),
    SUCCESS(0, "ok",""),
    NO_MATCHING_USER(40400, "无匹配用户", "数据库中没有找到符合条件的用户"),
    SYSTEM_ERROR(50000, "系统内部错误", "服务器处理请求时发生未知错误");

        // 构造方法和 Getter 保持不变


    ;


    private final int code;
    /**
     * 状态码信息
     */
    private final String message;
    /**
     * 状态码描述
     */
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;

    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
}
