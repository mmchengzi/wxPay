package com.mast.wxpay.mapper;

import com.mast.wxpay.entity.Refund;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
public interface RefundMapper {
    int delete(Map<String,Object> map);
    int insert(Refund record);
    int update(Refund record);
    List<Refund> getRefund(Map<String,Object> map);
}