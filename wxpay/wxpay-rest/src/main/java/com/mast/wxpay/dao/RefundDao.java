package com.mast.wxpay.dao;

import com.mast.wxpay.entity.Refund;

import java.util.List;

public interface RefundDao {
	int delete(String no,Integer id);
	int insert(Refund refund);
	int update(Refund refund);
	List<Refund> getRefund(String no,Integer id,Integer oid,String userid);
}
