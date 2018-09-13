package com.mast.wxpay.mapper;

import com.mast.wxpay.entity.Order;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface OrderMapper {
    List<Order> getOrder(Map<String,Object> map);
    int delete(Map<String,Object> map);
    int insert(Order record);
    int update(Order record);
}