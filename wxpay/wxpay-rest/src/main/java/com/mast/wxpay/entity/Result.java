package com.mast.wxpay.entity;

import lombok.Data;

/**
 * @author Administrator
 * @date 2017-7-5
 */
@Data
public class Result {

    private Integer code;

    private String msg;

    private Object data;
}
