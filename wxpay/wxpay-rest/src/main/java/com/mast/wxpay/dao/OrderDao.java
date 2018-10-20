package com.mast.wxpay.dao;

import com.mast.wxpay.entity.Order;

import java.util.List;

public interface OrderDao {
	List<Order> getOrder(String no,String userid,Long id);
	int delete(String no,Long id);
	int insert(Order record);
	int update(Order record);
}
