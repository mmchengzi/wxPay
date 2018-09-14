package com.mast.wxpay.dao;

import com.mast.wxpay.entity.Order;

import java.util.List;
import java.util.Map;

public interface OrderDao {
	List<Order> getOrder(String no,String userid,Integer id);
	int delete(String no,Integer id);
	int insert(Order record);
	int update(Order record);
}
