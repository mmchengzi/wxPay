package com.mast.wxpay.mapper;

import com.mast.wxpay.entity.Order;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderMapper {
    Order findFirstByNoAndType(String no);
}