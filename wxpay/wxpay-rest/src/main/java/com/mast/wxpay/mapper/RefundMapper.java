package com.mast.wxpay.mapper;

import com.mast.wxpay.entity.Refund;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface RefundMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Refund record);

    int insertSelective(Refund record);

    Refund selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Refund record);

    int updateByPrimaryKey(Refund record);

    /**
     * 根据源订单号id 查询是否有退款订单 没有就返回空 有就汇总之前退款订单金额返回
     *
     * @param oid Integer 订单号id
     * @return BigDecimal
     */
    BigDecimal sumRefundPriceByOid(Integer oid);
}