package com.mast.wxpay.controller;


import com.mast.wxpay.entity.Result;
import com.mast.wxpay.service.WxService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;

/**
*User zjc
*Created with IntelliJ IDEA
*Created on 2018-09-13 14:53
*
*/
@RestController
@RequestMapping(value = "wxPay")
@Log
@Api(value="WxController",description="微信支付")
public class WxController {


	@Autowired
	private WxService payService;

	/**
	 * 退款查询
	 * @param userid
	 * @param no
	 * @param id
	 * @param request
	 * @return
	 */
	@ApiOperation(value = "退款查询", notes = "退款查询")
	@RequestMapping(value = "getRefund", method = RequestMethod.POST)
	@ResponseBody
	public Result getRefund(@RequestParam(name = "userid") String userid,
							  @RequestParam(name = "no") String no,
							  @RequestParam(name = "id") Integer id,
							@RequestParam(name = "oid") Integer oid,
							  HttpServletRequest request) {
		return payService.getRefund( no, id, oid, userid);
	}
	/**
	 * 订单查询
	 * @param userid
	 * @param no
	 * @param id
	 * @param request
	 * @return
	 */
	@ApiOperation(value = "订单查询", notes = "订单查询")
	@RequestMapping(value = "getOrder", method = RequestMethod.POST)
	@ResponseBody
	public Result getOrder(@RequestParam(name = "userid") String userid,
						   @RequestParam(name = "no") String no,
						   @RequestParam(name = "id") Integer id,
						   HttpServletRequest request) {
		return payService.getOrder(no,userid, id);
	}

	/**
	 * 统一下单
	 *
	 * @param goodName String 商品名称
	 * @param price    BigDecimal 金额
	 * @param request  HttpServletRequest
	 * @return Result
	 * @throws Exception Exception
	 */
	@ApiOperation(value = "统一下单", notes = "统一下单")
	@RequestMapping(value = "create", method = RequestMethod.POST)
	@ResponseBody
	public Result createOrder(@RequestParam(name = "userid") String userid,
							  @RequestParam(name = "goodName") String goodName,
							  @RequestParam(name = "price") BigDecimal price,
							  HttpServletRequest request) {
		return payService.createOrder(userid,goodName, price, request);
	}

	/**
	 * 成功支付回调
	 *
	 * @param request  HttpServletRequest
	 * @param response HttpServletResponse
	 * @return String
	 * @throws Exception Exception
	 */
	@ApiOperation(value = "成功支付回调", notes = "成功支付回调")
	@RequestMapping(value = "success", method = RequestMethod.POST)
	@ResponseBody
	public String success(HttpServletRequest request, HttpServletResponse response) {
		return payService.success(request, response);
	}

	/**
	 * 统一下单查询支付状态
	 *
	 * @param no String 订单号
	 * @return Result
	 */
	@ApiOperation(value = "统一下单查询支付状态", notes = "统一下单查询支付状态")
	@RequestMapping(value = "status", method = RequestMethod.POST)
	@ResponseBody
	public Result orderStatus(@RequestParam("order") String no) {

		return payService.findPayStatus(no);
	}

	/**
	 * 退款
	 *
	 * @param no    String 订单号
	 * @param price BigDecimal 金额
	 * @return Result
	 * @throws Exception Exception
	 */
	@ApiOperation(value = "退款", notes = "退款")
	@RequestMapping(value = "refund", method = RequestMethod.POST)
	@ResponseBody
	public Result refundPos(@RequestParam(name = "order") String no,
							@RequestParam(name = "price") BigDecimal price) {
		return payService.refund(no, price);
	}

	/**
	 * 订单关闭
	 * @param no String 订单号
	 * @return Result
	 * @throws Exception Exception
	 */
	@ApiOperation(value = "订单关闭", notes = "订单关闭")
	@RequestMapping(value = "close", method = RequestMethod.POST)
	@ResponseBody
	public Result orderClose(@RequestParam(name = "order") String no) {
		return payService.orderClose(no);

	}

}
