package com.mast.wxpay.entity;

import lombok.Data;
import lombok.extern.log4j.Log4j;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class Refund {
    private Integer id;

    private Integer oid;
    private String userid;

    private String no;

    private BigDecimal price;

    private Date updateTime;

    private Date createTime;
}