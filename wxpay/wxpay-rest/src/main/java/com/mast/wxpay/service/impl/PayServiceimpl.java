package com.mast.wxpay.service.impl;


import com.mast.wxpay.dao.OrderDao;
import com.mast.wxpay.dao.RefundDao;
import com.mast.wxpay.entity.Order;
import com.mast.wxpay.entity.Refund;
import com.mast.wxpay.entity.Result;
import com.mast.wxpay.service.WxService;
import com.mast.wxpay.util.WxUtil;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User zjc
 * Created with IntelliJ IDEA
 * Created on 2018-09-12 11:30
 */
@Service
@Log
public class PayServiceimpl implements WxService {
	@Autowired
	private OrderDao orderRepository;

	@Autowired
	private RefundDao refundRepository;

	@Value("${wx.appId}")
	private String appId;
	@Value("${wx.apiKey}")
	private String apiKey;
	@Value("${wx.mchId}")
	private String mchId;
	@Value("${wx.p12}")
	private String p12;
	@Value("${wx.notiftUrl}")
	private String notiftUrl;
	@Value("${wx.ufdoderUrl}")
	private String ufdoderUrl;
	@Value("${wx.qrCodePath}")
	private String qrCodePath;
	@Value("${wx.closeorder}")
	private String closeorder;

	/**
	 * 查询支付成功
	 *
	 * @param no
	 * @return
	 */
	@Override
	public Result findPayStatus(String no) {

		Result result = new Result();
		List<Order> order = orderRepository.getOrder(no, null, null);
		if (order == null) {
			result.setCode(1);
			result.setData(null);
			result.setMsg("订单不存在");
			log.info("订单不存在");
			return result;
		}
		switch (order.get(0).getStatus()) {
			case 1:
				result.setCode(1);
				result.setData(null);
				result.setMsg("订单未支付");
				log.info("订单未支付");
				break;
			case 2:
				result.setCode(0);
				result.setData(null);
				result.setMsg("订单已支付");
				log.info("订单已支付");
				break;
			case 3:
				result.setCode(1);
				result.setData(null);
				result.setMsg("订单已关闭");
				log.info("订单已关闭");
				break;
			default:
				result.setCode(1);
				result.setData(null);
				result.setMsg("未知订单类型");
				log.info("未知订单类型");
				break;
		}
		return result;
	}

	/**
	 * 退款
	 *
	 * @param no
	 * @param price
	 * @return
	 */
	@Override
	public Result refund(String no, BigDecimal price) {
		Result result = new Result();
		List<Order> order = orderRepository.getOrder(no, null, null);
		if (order == null) {

			result.setCode(1);
			result.setData(null);
			result.setMsg("订单查找失败");
			log.info("订单查找失败");
			return result;
		}
		if (order.get(0).getStatus().equals(1) || order.get(0).getStatus().equals(3)) {
			result.setCode(1);
			result.setData(null);
			result.setMsg("订单未支付或已关闭");
			log.info("订单未支付或已关闭");
			return result;
		}
		//根据源订单号id 查询是否有退款订单 没有就返回空 有就汇总之前退款订单金额返回
		List<Refund> refund = refundRepository.getRefund(null, null, order.get(0).getId(), null);

		BigDecimal sumRefundPrice = null;
		for (int i = 0; i < refund.size(); i++) {
			sumRefundPrice = sumRefundPrice.add(refund.get(i).getPrice());
		}

		if (sumRefundPrice != null) {
			if (Integer.valueOf(price.compareTo(order.get(0).getPrice().subtract(sumRefundPrice))).equals(1)) {
				result.setCode(1);
				result.setData(null);
				result.setMsg("退款金额大于剩余退款金额");
				log.info("退款金额大于剩余退款金额");
				return result;
			}
		} else {

			if (Integer.valueOf(price.compareTo(order.get(0).getPrice())).equals(1)) {
				result.setCode(1);
				result.setData(null);
				result.setMsg("退款金额大于订单金额");
				log.info("退款金额大于订单金额");
				return result;
			}
		}
		return this.wxRefund(order.get(0), price);

	}


	public Result wxRefund(Order order, BigDecimal price) {
		Result result = new Result();
		try {

			String nonceStr = UUID.randomUUID().toString().split("-")[0];
			//  转分
			Integer goodPrice = price.multiply(new BigDecimal(100)).intValue();
			SimpleDateFormat dateForMater = new SimpleDateFormat("yyyyMMdd");
			Date date = new Date();
			String today = dateForMater.format(date);
			String refundNo = today + String.valueOf(System.currentTimeMillis()) + Double.valueOf(Math.random() * 10000).intValue();
			Map<String, String> dataMap = new HashMap<>(16);
			dataMap.put("appid", appId);
			dataMap.put("mch_id", mchId);
			dataMap.put("nonce_str", nonceStr);
			dataMap.put("out_trade_no", order.getNo());
			dataMap.put("out_refund_no", refundNo);
			dataMap.put("total_fee", String.valueOf((order.getPrice().multiply(new BigDecimal(100))).intValue()));
			dataMap.put("refund_fee", goodPrice.toString());
			String sendXml = new WxUtil().wxEnStr(dataMap, apiKey);
			String url = "https://api.mch.weixin.qq.com/secapi/pay/refund";
			Map<String, String> resultMap = new WxUtil().requestWx(sendXml, url, p12, mchId);
			if ("FAIL".equals(resultMap.get("return_code"))) {

				log.warning("请求退款返回失败");
				result.setMsg("请求退款失败,信息:" + resultMap.get("return_msg"));
				result.setData(null);
				result.setCode(1);
				return result;
			}
			if (new WxUtil().validatorSign(resultMap, apiKey).equals(false)) {
				log.warning("数据验签失败");
				result.setMsg("数据验签失败");
				result.setData(null);
				result.setCode(1);
				return result;
			}
			Refund refund = new Refund();
			refund.setNo(refundNo);
			refund.setOid(order.getId());
			refund.setUserid(order.getUserid());
			refund.setPrice(price);
			refund.setCreatedat(new Long(System.currentTimeMillis() / 1000).intValue());
			refund.setUpdatedat(refund.getCreatedat());
			refundRepository.insert(refund);
			List<Refund> refundlist = refundRepository.getRefund(null, null, order.getId(), null);
			BigDecimal sumRefundPrice = null;
			for (int i = 0; i < refundlist.size(); i++) {
				sumRefundPrice = sumRefundPrice.add(refundlist.get(i).getPrice());
			}
			if (sumRefundPrice.doubleValue() >= order.getPrice().doubleValue()) {
				order.setStatus(3);
				orderRepository.update(order);
			}
			result.setMsg("success");
			result.setData(null);
			result.setCode(0);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.setMsg("success");
			result.setData(null);
			result.setCode(0);
			return result;
		}

	}

	@Override
	public Result createOrder(String userid, String goodName, BigDecimal price, HttpServletRequest request) {
		Result result = new Result();
		try {
			SimpleDateFormat dateForMater = new SimpleDateFormat("yyyyMMdd");
			Date date = new Date();
			String today = dateForMater.format(date);
			Calendar ca = Calendar.getInstance();
			ca.setTime(date);
			ca.add(Calendar.MINUTE, 5);
			String orderNo = today + String.valueOf(System.currentTimeMillis()) + Double.valueOf(Math.random() * 10000).intValue();
			//  转分
			Integer goodPrice = price.multiply(new BigDecimal(100)).intValue();
			String nonceStr = UUID.randomUUID().toString().split("-")[0];
			Map<String, String> data = new HashMap<>(16);
			data.put("appid", appId);
			data.put("mch_id", mchId);
			data.put("nonce_str", nonceStr);
			data.put("body", new String(goodName.getBytes("UTF-8"), "ISO-8859-1"));
			data.put("out_trade_no", orderNo);
			data.put("total_fee", goodPrice.toString());
			data.put("spbill_create_ip", request.getRemoteAddr());
			data.put("time_start", new SimpleDateFormat("yyyyMMddHHmmss").format(date));
			data.put("time_expire", new SimpleDateFormat("yyyyMMddHHmmss").format(ca.getTime()));
			data.put("notify_url", notiftUrl);
			data.put("trade_type", "NATIVE");
			data.put("product_id", new String(goodName.getBytes("UTF-8"), "ISO-8859-1"));

			String sendXml = new WxUtil().wxEnStr(data, apiKey);
			Map<String, String> resultMap = new WxUtil().requestWx(sendXml, ufdoderUrl, p12, mchId);
			if ("FAIL".equals(resultMap.get("return_code"))) {

				log.warning("Request wx fail");
				log.warning(resultMap.get("return_msg"));
				result.setCode(-3);
				result.setData(null);
				result.setMsg("请求返回 :" + resultMap.get("return_code") + resultMap.get("return_msg"));
				return result;
			}
			if ((new WxUtil().validatorSign(resultMap, apiKey)).equals(false)) {
				log.warning("数据验签失败");
			}
			Order order = new Order();
			order.setNo(orderNo);
			order.setGoodname(goodName);
			order.setPrice(price);
			//订单创建
			order.setStatus(1);
			order.setUserid(userid);
			order.setDate(new Date());
			order.setCreatedat(new Long(System.currentTimeMillis() / 1000).intValue());
			order.setUpdatedat(order.getCreatedat());
			orderRepository.insert(order);
			result.setCode(0);
			Map<String, String> resMap = new HashMap<>(16);
			resMap.put("order_no", orderNo);
			resMap.put("url", qrCodePath + "?code=" + Base64.getEncoder().encodeToString(resultMap.get("code_url").getBytes()));
			result.setData(resMap);
			result.setMsg("success");
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.setCode(1);
			result.setData(null);
			result.setMsg(e.getMessage());
			return result;
		}
	}

	@Override
	public Result getOrder(String no, String userid, Integer id) {
		Result result = new Result();
		try {
			List<Order> orders = orderRepository.getOrder(no, userid, id);
			result.setCode(0);
			result.setData(orders);
			result.setMsg("success");
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.setCode(1);
			result.setData(null);
			result.setMsg(e.getMessage());
			return result;
		}
	}

	@Override
	public Result getRefund(String no, Integer id, Integer oid, String userid) {
		Result result = new Result();
		try {
			List<Refund> refund = refundRepository.getRefund(no, id, oid, userid);
			result.setCode(0);
			result.setData(refund);
			result.setMsg("success");
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.setCode(1);
			result.setData(null);
			result.setMsg(e.getMessage());
			return result;
		}
	}

	@Override
	public String success(HttpServletRequest request, HttpServletResponse response) {
		try {
			response.addHeader("Content-Type", "application/xml");
			response.addHeader("charset", "utf-8");
			BufferedReader reader = request.getReader();
			StringBuilder inputString = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {

				inputString.append(line);
			}
			String xmlStr = inputString.toString();
			Map<String, String> resultMap = new WxUtil().xmlToMap(xmlStr);
			if (new WxUtil().validatorSign(resultMap, apiKey).equals(false)) {

				log.warning("回调成功数据验签失败");
				return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[FAIL]]></return_msg></xml>";
			}

			if("SUCCESS".equals((String)resultMap.get("result_code"))){
				List<Order> orderlist = orderRepository.getOrder(resultMap.get("out_trade_no"), null, null);
				if (orderlist == null) {
					//退款
					BigDecimal settlement_total_fee = new BigDecimal(resultMap.get("settlement_total_fee"));
					Result result=	this.refund(resultMap.get("out_trade_no"),settlement_total_fee);
					if(result!=null &&result.getCode()==0){
						log.info("退款成功");
					}else {
						log.warning("退款失败：" + result.getMsg());
					}
					log.warning("返回订单没有找到：" + resultMap.get("out_trade_no"));
					log.warning("准备返回微信：[CDATA[FAIL]]");
					return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[FAIL]]></return_msg></xml>";
				}
				Order order = orderlist.get(0);
				order.setUpdatedat(new Long(System.currentTimeMillis() / 1000).intValue());
				order.setStatus(2);
				orderRepository.update(order);
				return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";

			}else{
				return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[FAIL]]></return_msg></xml>";
			}

		} catch (Exception e) {
			e.printStackTrace();
			return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[FAIL]]></return_msg></xml>";
		}


	}


	@Override
	public Result orderClose(String no) {
		Result result = new Result();
		try {
			List<Order> orderlsit = orderRepository.getOrder(no, null, null);
			Order order = orderlsit.get(0);
			if (order == null) {
				log.warning("订单不存在");
				result.setMsg("订单不存在");
				result.setCode(1);
				result.setData(null);
				return result;

			}
			if (order.getStatus() != 1) {
				log.warning("订单已支付或已关闭");
				result.setMsg("订单已支付或已关闭");
				result.setCode(1);
				result.setData(null);
				return result;

			}
			String nonceStr = UUID.randomUUID().toString().split("-")[0];
			Map<String, String> data = new HashMap<>(16);
			data.put("appid", appId);
			data.put("mch_id", mchId);
			data.put("out_trade_no", no);
			data.put("nonce_str", nonceStr);
			String sendXml = new WxUtil().wxEnStr(data, apiKey);

			Map<String, String> resultMap = new WxUtil().requestWx(sendXml, closeorder, p12, mchId);
			if ("FAIL".equals(resultMap.get("return_code"))) {
				log.warning("返回错误");
				result.setMsg(resultMap.get("return_msg"));
				result.setCode(1);
				result.setData(null);
				return result;
			}
			if (new WxUtil().validatorSign(resultMap, apiKey).equals(false)) {
				log.warning("数据验签失败");
				result.setMsg(resultMap.get("return_msg"));
				result.setCode(1);
				result.setData(null);
				return result;
			}
			order.setStatus(3);
			orderRepository.insert(order);
			result.setMsg("success");
			result.setCode(0);
			result.setData(null);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.setMsg(e.getMessage());
			result.setCode(1);
			result.setData(null);
			return result;
		}
	}
}
