package com.mast.wxpay.entity;

import lombok.Data;
import lombok.extern.log4j.Log4j;

import java.math.BigDecimal;
import java.util.Date;
@Data
public class Order {
    private Integer id;

    private String  no;
    private String userid;
    private String goodname;

    private BigDecimal price;

    private Integer status;


    private Date createTime;

    private Date updatedTime;
}