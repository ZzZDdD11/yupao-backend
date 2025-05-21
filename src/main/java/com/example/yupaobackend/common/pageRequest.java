package com.example.yupaobackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页请求参数
 */

@Data
public class pageRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 分页大小
     */
    protected int pageSize;

    /**
     * 当前是第几页
     */
    protected int pageNum;
}
