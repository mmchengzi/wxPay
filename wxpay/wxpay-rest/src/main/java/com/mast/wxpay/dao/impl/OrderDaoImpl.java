package com.mast.wxpay.dao.impl;

import com.mast.wxpay.dao.OrderDao;
import com.mast.wxpay.entity.Order;
import com.mast.wxpay.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
@Repository
public class OrderDaoImpl implements OrderDao {
	@Autowired
	OrderMapper service;

	@Override
	public List<Order> getOrder(String no, String userid,Integer id) {
		Map map = new HashMap();
		map.put("no", no);
		map.put("id", id);
		map.put("userid", userid);
		return service.getOrder(map);
	}

	@Override
	public int delete(String no, Integer id) {
		Map map = new HashMap();
		map.put("no", no);
		map.put("id", id);
		return service.delete(map);
	}

	@Override
	public int insert(Order Order) {
		return service.insert(Order);
	}

	@Override
	public int update(Order Order) {
		return service.update(Order);
	}

}
