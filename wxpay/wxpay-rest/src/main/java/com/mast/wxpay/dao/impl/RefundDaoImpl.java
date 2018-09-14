package com.mast.wxpay.dao.impl;

import com.mast.wxpay.dao.RefundDao;
import com.mast.wxpay.entity.Order;
import com.mast.wxpay.entity.Refund;
import com.mast.wxpay.mapper.OrderMapper;
import com.mast.wxpay.mapper.RefundMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@SuppressWarnings("rawtypes")
@Repository
public class RefundDaoImpl implements RefundDao {
	@Autowired
	RefundMapper service;

	@Override
	public int delete(String no, Integer id) {
		Map map = new HashMap();
		map.put("no", no);
		map.put("id", id);
		return service.delete(map);
	}

	@Override
	public int insert(Refund refund) {
		return service.insert(refund);
	}

	@Override
	public int update(Refund refund) {
		return service.update(refund);
	}

	@Override
	public List<Refund> getRefund(String no, Integer id, Integer oid,String userid) {
		Map map = new HashMap();
		map.put("no", no);
		map.put("oid", oid);
		map.put("id", id);
		map.put("userid", userid);
		return service.getRefund(map);
	}
}
