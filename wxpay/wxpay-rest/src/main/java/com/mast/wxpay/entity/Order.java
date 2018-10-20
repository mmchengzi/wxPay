package com.mast.wxpay.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
@Data
public class Order {
    private Long id;

    private String no;

    private String userId;

    private Integer status;

    private BigDecimal price;

    private String goodName;

    private Date createTime;

    private Date updatedTime;
}