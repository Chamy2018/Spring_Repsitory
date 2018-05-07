package com.chamy.jd.service;

import com.chamy.jd.pojo.Result;

public interface SearchService {
	
	/**
	 * 搜索商品
	 * 参数确定方式：根据提交的表单元素来确定
	 */
	Result searchProduct(String queryString,String catalog_name,String price,Integer page,String sort);


}
