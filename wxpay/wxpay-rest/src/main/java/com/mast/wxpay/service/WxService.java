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
	public Result createOrder(String userid,String goodName, BigDecimal price, HttpServletRequest request);
	//订单查询
	public Result getOrder(String no,String userid,Integer id);
	//退款查询
	public Result getRefund(String no,Integer id,Integer oid,String userid);
	//成功回调接口
	public String success(HttpServletRequest request, HttpServletResponse response);

	//订单关闭
	public Result orderClose( String no);
}
