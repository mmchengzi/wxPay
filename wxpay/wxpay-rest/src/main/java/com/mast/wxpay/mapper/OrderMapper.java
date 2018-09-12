package com.mast.wxpay.mapper;

import com.mast.wxpay.entity.Order;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderMapper {
    Order findFirstByNoAndType(String no);
    int deleteByPrimaryKey(Integer id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);
}