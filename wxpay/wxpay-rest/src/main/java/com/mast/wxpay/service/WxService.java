package com.mast.wxpay.service;

import com.mast.wxpay.entity.Result;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;

public interface WxService {
//查看订单状态
	public Result findPayStatus(String no);
	//退款
	public Result refund(String no, BigDecimal price);

	//创建订单
	public Result createOrder(String goodName, BigDecimal price, HttpServletRequest request);

	//成功回调接口
	public String success(HttpServletRequest request, HttpServletResponse response);


	//订单关闭
	public Result orderClose( String no);
}
