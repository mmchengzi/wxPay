package com.mast.wxpay.controller;


import lombok.extern.java.Log;
import me.zpq.config.WxPayConfig;
import me.zpq.exception.DataSignException;
import me.zpq.exception.LogicException;
import me.zpq.pojo.Order;
import me.zpq.pojo.Result;
import me.zpq.repository.OrderRepository;
import me.zpq.service.PayService;
import me.zpq.util.WxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Administrator
 * @date 2017-7-5
 */
@Controller
@RequestMapping(value = "wxPay")
@Log
public class WxController {

    @Autowired
    private WxPayConfig wxPayConfig;

    @Value("http://${serverIp}:${server.port}")
    private String domain;

    @Autowired
    private PayService payService;

    @Autowired
    private OrderRepository orderRepository;

    /**
     * 统一下单
     *
     * @param goodName String 商品名称
     * @param price    BigDecimal 金额
     * @param request  HttpServletRequest
     * @return Result
     * @throws Exception Exception
     */
    @RequestMapping(value = "create", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor = LogicException.class)
    public Result createOrder(@RequestParam(name = "goodName") String goodName,
                              @RequestParam(name = "price") BigDecimal price,
                              HttpServletRequest request) throws Exception {
        Result result = new Result();
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
        data.put("appid", wxPayConfig.getAppId());
        data.put("mch_id", wxPayConfig.getMchId());
        data.put("nonce_str", nonceStr);
        data.put("body", new String(goodName.getBytes("UTF-8"), "ISO-8859-1"));
        data.put("out_trade_no", orderNo);
        data.put("total_fee", goodPrice.toString());
        data.put("spbill_create_ip", request.getRemoteAddr());
        data.put("time_start", new SimpleDateFormat("yyyyMMddHHmmss").format(date));
        data.put("time_expire", new SimpleDateFormat("yyyyMMddHHmmss").format(ca.getTime()));
        data.put("notify_url", this.domain + "/wxPay/success");
        data.put("trade_type", "NATIVE");
        data.put("product_id", new String(goodName.getBytes("UTF-8"), "ISO-8859-1"));
        String sendXml = new WxUtil().wxEnStr(data, wxPayConfig.getKey());
        String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        Map<String, String> resultMap = new WxUtil().requestWx(sendXml, url, wxPayConfig.getP12(), wxPayConfig.getMchId());
        if ("FAIL".equals(resultMap.get("return_code"))) {

            logger.warn("Request wx fail");
            logger.warn(resultMap.get("return_msg"));
            result.setCode(-3);
            result.setData(null);
            result.setMsg("请求返回 :" + resultMap.get("return_code") + resultMap.get("return_msg"));
            return result;
        }
        if (new WxUtil().validatorSign(resultMap, wxPayConfig.getKey()).equals(false)) {

            throw new DataSignException("数据验签失败");
        }
        Order order = new Order();
        order.setNo(orderNo);
        order.setGoodName(goodName);
        order.setPrice(price);
        //微信
        order.setType(1);
        //订单创建
        order.setStatus(1);
        order.setDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        order.setCreatedAt(new Long(System.currentTimeMillis() / 1000).intValue());
        order.setUpdatedAt(order.getCreatedAt());
        orderRepository.save(order);
        result.setCode(0);
        Map<String, String> resMap = new HashMap<>(16);
        resMap.put("order_no", orderNo);
        resMap.put("url", this.domain + "/wxPay/qrCode?code=" + Base64.getEncoder().encodeToString(resultMap.get("code_url").getBytes()));
        result.setData(resMap);
        result.setMsg("success");
        return result;
    }

    /**
     * 成功支付回调
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @return String
     * @throws Exception Exception
     */
    @RequestMapping(value = "success", method = RequestMethod.POST)
    @ResponseBody
    public String success(HttpServletRequest request, HttpServletResponse response) throws Exception {

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
        if (new WxUtil().validatorSign(resultMap, wxPayConfig.getKey()).equals(false)) {

            logger.warn("回调成功数据验签失败");
            return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[FAIL]]></return_msg></xml>";
        }
        Order order = orderRepository.findFirstByNoAndType(resultMap.get("out_trade_no"), 1);
        if (order == null) {

            logger.warn("返回订单没有找到：" + resultMap.get("out_trade_no"));
            logger.warn("准备返回微信：[CDATA[FAIL]]");
            return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[FAIL]]></return_msg></xml>";
        }
        order.setUpdatedAt(new Long(System.currentTimeMillis() / 1000).intValue());
        order.setStatus(2);
        orderRepository.saveAndFlush(order);
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";

    }

    /**
     * 统一下单查询支付状态
     *
     * @param no String 订单号
     * @return Result
     * @throws LogicException LogicException
     */
    @RequestMapping(value = "status", method = RequestMethod.POST)
    @ResponseBody
    public Result orderStatus(@RequestParam("order") String no) throws LogicException {

        return payService.findPayStatus(no, PayService.PayType.WX_PAY);
    }

    /**
     * 退款
     *
     * @param no    String 订单号
     * @param price BigDecimal 金额
     * @return Result
     * @throws Exception Exception
     */
    @RequestMapping(value = "refund", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor = LogicException.class)
    public Result refundPos(@RequestParam(name = "order") String no,
                            @RequestParam(name = "price") BigDecimal price) throws Exception {
        return payService.refund(no, price, PayService.PayType.WX_PAY);
    }

    /**
     * 订单关闭
     *
     * @param no String 订单号
     * @return Result
     * @throws Exception Exception
     */
    @RequestMapping(value = "close", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor = LogicException.class)
    public Result orderClose(@RequestParam(name = "order") String no) throws Exception {

        Result result = new Result();
        Order order = orderRepository.findFirstByNoAndType(no, 1);
        if (order == null) {

            throw new LogicException("订单不存在");
        }
        if (order.getStatus() != 1) {

            throw new LogicException("订单已支付或已关闭");
        }
        String nonceStr = UUID.randomUUID().toString().split("-")[0];
        Map<String, String> data = new HashMap<>(16);
        data.put("appid", wxPayConfig.getAppId());
        data.put("mch_id", wxPayConfig.getMchId());
        data.put("out_trade_no", no);
        data.put("nonce_str", nonceStr);
        String sendXml = new WxUtil().wxEnStr(data, wxPayConfig.getKey());
        String url = "https://api.mch.weixin.qq.com/pay/closeorder";
        Map<String, String> resultMap = new WxUtil().requestWx(sendXml, url, wxPayConfig.getP12(), wxPayConfig.getMchId());
        if ("FAIL".equals(resultMap.get("return_code"))) {

            logger.warn("返回错误");
            result.setMsg(resultMap.get("return_msg"));
            result.setCode(-3);
            result.setData(null);
            return result;
        }
        if (new WxUtil().validatorSign(resultMap, wxPayConfig.getKey()).equals(false)) {

            throw new DataSignException("数据验签失败");
        }
        order.setStatus(3);
        orderRepository.saveAndFlush(order);
        result.setMsg("success");
        result.setCode(0);
        result.setData(null);
        return result;
    }

}
