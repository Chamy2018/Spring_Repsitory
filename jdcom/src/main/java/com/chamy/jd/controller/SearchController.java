package com.chamy.jd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.chamy.jd.pojo.Result;
import com.chamy.jd.service.SearchService;

@Controller
public class SearchController {
	
	@Autowired
	private SearchService searchService;
	
/*	@RequestMapping("/list.action")
	public String list() {
		return "product_list";
	}*/
	
	/**
	 * 搜索商品数据
	 * 	action="list.action" 
	 */
	@RequestMapping("/list.action")
	public String list(Model model,String queryString,String catalog_name,String price,
			Integer page,String sort){
		Result result = this.searchService.searchProduct(queryString, catalog_name, price, page, sort);
		
		//响应搜索的模型数据
		model.addAttribute("result", result);
		
		// 参数数据回显
		model.addAttribute("queryString",queryString);
		model.addAttribute("catalog_name",catalog_name);
		model.addAttribute("price",price);
		model.addAttribute("sort",sort);
		

		
		return "product_list";
	}


}
