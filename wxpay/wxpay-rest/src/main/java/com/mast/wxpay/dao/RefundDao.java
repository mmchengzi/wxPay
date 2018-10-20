package com.mast.wxpay.dao;

import com.mast.wxpay.entity.Refund;

import java.util.List;

public interface RefundDao {
	int delete(String no,Long id);
	int insert(Refund refund);
	int update(Refund refund);
	List<Refund> getRefund(String no,Long id,Long oid,String userid);
}
