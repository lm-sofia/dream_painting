package com.aiproject.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 业务错误码，与 database/API_CONTRACT.md 第 2 节错误码表一一对应。
 */
@Getter
public enum ErrorCode {

    BAD_REQUEST(40001, HttpStatus.BAD_REQUEST, "参数校验失败"),
    UNAUTHORIZED(40101, HttpStatus.UNAUTHORIZED, "未认证或凭证无效"),
    FORBIDDEN(40301, HttpStatus.FORBIDDEN, "无权限执行此操作"),
    NOT_FOUND(40401, HttpStatus.NOT_FOUND, "资源不存在"),
    CONFLICT(40901, HttpStatus.CONFLICT, "资源冲突"),
    /** 创作额度不足（第 4 课：扣费场景） */
    INSUFFICIENT_QUOTA(40201, HttpStatus.PAYMENT_REQUIRED, "创作额度不足"),
    INTERNAL_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "服务器内部错误");

    private final int code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(int code, HttpStatus httpStatus, String defaultMessage) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }
}
