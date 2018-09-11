package com.mast.wxpay.service.impl;


import com.mast.wxpay.entity.Order;
import com.mast.wxpay.entity.Result;
import com.mast.wxpay.mapper.OrderMapper;
import com.mast.wxpay.mapper.RefundMapper;
import com.mast.wxpay.service.PayService;
import lombok.extern.java.Log;
import me.zpq.config.AliPayConfig;
import me.zpq.config.WxPayConfig;
import me.zpq.exception.DataSignException;
import me.zpq.exception.LogicException;
import me.zpq.pojo.Order;
import me.zpq.pojo.Refund;
import me.zpq.pojo.Result;
import me.zpq.repository.OrderRepository;
import me.zpq.repository.RefundRepository;
import me.zpq.util.AliUtil;
import me.zpq.util.WxUtil;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Administrator
 * @date 2017-12-15
 */
@Service
@Log
public class PayServiceimpl implements PayService {
    @Autowired
    private OrderMapper orderRepository;

    @Autowired
    private RefundMapper refundRepository;

    public Result findPayStatus(String no)  {

        Result result = new Result();
        Order order = orderRepository.findFirstByNoAndType(no);
        if (order == null) {
            result.setCode(-1);
            result.setData(null);
            result.setMsg("订单不存在");
            log.info("订单不存在");
            return result;
        }
        switch (order.getStatus()) {
            case 1:
                result.setCode(1);
                result.setData(null);
                result.setMsg("订单未支付");
                log.info("订单未支付");
            case 2:
                result.setCode(0);
                result.setData(null);
                result.setMsg("订单已支付");
                log.info("订单已支付");
                break;
            case 3:
                result.setCode(2);
                result.setData(null);
                result.setMsg("订单已关闭");
                log.info("订单已关闭");
            default:
                result.setCode(3);
                result.setData(null);
                result.setMsg("未知订单类型");
                log.info("未知订单类型");
        }
        return result;
    }

    public Result refund(String no, BigDecimal price)  {

        Order order = orderRepository.findFirstByNoAndType(no, payType.value);
        if (order == null) {

            throw new LogicException("订单查找失败");
        }
        if (order.getStatus().equals(1) || order.getStatus().equals(3)) {

            throw new LogicException("订单未支付或已关闭");
        }
        BigDecimal sumRefundPrice = refundRepository.sumRefundPriceByOid(order.getId());
        if (sumRefundPrice != null) {

            if (Integer.valueOf(price.compareTo(order.getPrice().subtract(sumRefundPrice))).equals(1)) {

                throw new LogicException("退款金额大于剩余退款金额");
            }
        } else {

            if (Integer.valueOf(price.compareTo(order.getPrice())).equals(1)) {

                throw new LogicException("退款金额大于订单金额");
            }
        }
        if (payType.equals(PayType.WX_PAY)) {

            return this.wxRefund(order, no, price);
        } else {

            return this.aliRefund(order, no, price);
        }

    }

    @Override
    public Result wxRefund(Order order, String no, BigDecimal price) {
        return null;
    }

    private Result wxRefund(Order order, String no, BigDecimal price)  {

        Result result = new Result();
        String nonceStr = UUID.randomUUID().toString().split("-")[0];
        //  转分
        Integer goodPrice = price.multiply(new BigDecimal(100)).intValue();
        SimpleDateFormat dateForMater = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        String today = dateForMater.format(date);
        String refundNo = today + String.valueOf(System.currentTimeMillis()) + Double.valueOf(Math.random() * 10000).intValue();
        Map<String, String> dataMap = new HashMap<>(16);
        dataMap.put("appid", wxPayConfig.getAppId());
        dataMap.put("mch_id", wxPayConfig.getMchId());
        dataMap.put("nonce_str", nonceStr);
        dataMap.put("out_trade_no", no);
        dataMap.put("out_refund_no", refundNo);
        dataMap.put("total_fee", String.valueOf((order.getPrice().multiply(new BigDecimal(100))).intValue()));
        dataMap.put("refund_fee", goodPrice.toString());
        String sendXml = new WxUtil().wxEnStr(dataMap, wxPayConfig.getKey());
        String url = "https://api.mch.weixin.qq.com/secapi/pay/refund";
        Map<String, String> resultMap = new WxUtil().requestWx(sendXml, url, wxPayConfig.getP12(), wxPayConfig.getMchId());
        if ("FAIL".equals(resultMap.get("return_code"))) {

            logger.warn("请求退款返回失败");
            result.setMsg("请求退款失败,信息:" + resultMap.get("return_msg"));
            result.setData(null);
            result.setCode(-2);
            return result;
        }
        if (new WxUtil().validatorSign(resultMap, wxPayConfig.getKey()).equals(false)) {

            throw new DataSignException("数据验签失败");
        }
        Refund refund = new Refund();
        refund.setNo(refundNo);
        refund.setOid(order.getId());
        refund.setPrice(price);
        refund.setCreatedAt(new Long(System.currentTimeMillis() / 1000).intValue());
        refund.setUpdatedAt(refund.getCreatedAt());
        refundRepository.save(refund);
        BigDecimal sumPrice = refundRepository.sumRefundPriceByOid(order.getId());
        if (sumPrice.doubleValue() >= order.getPrice().doubleValue()) {

            order.setStatus(3);
            orderRepository.saveAndFlush(order);
        }
        result.setMsg("success");
        result.setData(null);
        result.setCode(0);
        return result;
    }
}
