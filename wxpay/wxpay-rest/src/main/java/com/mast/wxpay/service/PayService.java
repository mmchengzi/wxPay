package com.mast.wxpay.service;

import com.mast.wxpay.entity.Order;
import com.mast.wxpay.entity.Result;

import java.math.BigDecimal;

public interface PayService {
    public Result findPayStatus(String no);
    public Result refund(String no, BigDecimal price);
    public Result wxRefund(Order order, String no, BigDecimal price);
}
