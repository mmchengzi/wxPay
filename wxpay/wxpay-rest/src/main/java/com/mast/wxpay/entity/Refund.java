package com.mast.wxpay.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
@Data
public class Refund {
    private Long id;

    private Long oId;

    private String userId;

    private String no;

    private BigDecimal price;

    private Date updateTime;

    private Date createTime;
}