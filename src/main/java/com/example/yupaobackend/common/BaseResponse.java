package com.example.yupaobackend.common;
import com.example.yupaobackend.common.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回类
 * @param <T>
 */
@Data
public class BaseResponse<T> implements Serializable {
    private int code;

    private T data;

    private String message;

    private String description;


    public BaseResponse(int code, T data, String message, String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }
    public BaseResponse(int code, T data, String message) {
        this(code, data, message, "");

    }
    public BaseResponse(int code, T data){
        this(code,data,"","");
    }

    public BaseResponse(com.example.yupaobackend.common.ErrorCode errorcode){
        this(errorcode.getCode(),null, errorcode.getMessage(), errorcode.getDescription());
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
