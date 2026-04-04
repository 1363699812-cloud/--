// src/main/java/com/wms/backend/common/Result.java
package com.wms.backend.common;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 成功
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    // 失败
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    // 失败 (带错误码)
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}
