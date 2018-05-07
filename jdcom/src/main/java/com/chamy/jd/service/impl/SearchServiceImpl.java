package com.chamy.jd.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chamy.jd.pojo.Product;
import com.chamy.jd.pojo.Result;
import com.chamy.jd.service.SearchService;

@Service
public class SearchServiceImpl implements SearchService {


	@Autowired
	private HttpSolrServer httpSolrServer;
	public Result searchProduct(String queryString, String catalog_name, String price, Integer page, String sort) {
		//创建查询对象SolrQuery
		SolrQuery sq=new SolrQuery();
		
		//如果搜索关键字为空,搜索全部,否则搜索指定的关键词
		if(StringUtils.isNotBlank(queryString)) {
			sq.setQuery(queryString);
		}else {
			sq.setQuery("*:*");
		}
		
		//设置默认搜索域
		sq.set("df","product_keywords");
		
		//设置过滤条件
		if (StringUtils.isNotBlank(catalog_name)) {
			catalog_name="product_catalog_name:"+catalog_name;
		}
		if (StringUtils.isNotBlank(price)) {
			String[] arr = price.split("-");
			price="product_price:["+arr[0]+" TO "+arr[1]+"]";
		}
		sq.setFilterQueries(catalog_name,price);
		
		if (page==null) {
			page=1;
		}
		int pageSize=10;
		sq.setStart((page-1)*pageSize);
		sq.setRows(pageSize);
		if ("1".equals(sort)) {
			sq.setSort("product_price",ORDER.asc);
		}else {
			sq.setSort("product_price",ORDER.desc);
		}
		// 1.6.设置商品名称高亮显示
		sq.setHighlight(true);// 开启高亮显示
		sq.addHighlightField("product_name");// 添加高亮显示的域
		sq.setHighlightSimplePre("<font color='red'>");// 设置高亮显示html标签的前缀
		sq.setHighlightSimplePost("</font>");// 设置高亮显示的html标签的后缀
		
		//使用httpServer执行搜索
		QueryResponse queryResponse=null;
		try {
			queryResponse=this.httpSolrServer.query(sq);
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		
		//使用QueryResponse对象,获取搜索的数据
		SolrDocumentList docList=queryResponse.getResults();
		Map<String, Map<String, List<String>>> highlighting = queryResponse.getHighlighting();
		
		//处理结果集
		Result result=new Result();
		result.setCurPage(page);
		
		int totals=(int) docList.getNumFound();
		int pageCount=0;
		if (totals%pageSize==0) {
			pageCount=totals/pageSize;
		}else {
			pageCount=(totals/pageSize)+1;
		}
		
		//设置搜索结果集
		result.setPageCount(pageCount);
		result.setRecordCount(totals);
		
		List<Product> productList = new ArrayList<Product>();
		for (SolrDocument doc : docList) {
			//创建商品对象
			Product pro=new Product();
			String pid=doc.get("id").toString();
			String pname="";
			List<String> list = highlighting.get(pid).get("product_name");
			
			if(list != null && list.size()>0){
				pname = list.get(0);
			}else{
				pname=doc.get("product_name").toString();
			}
			
			// 商品图片
			String ppicture = doc.get("product_picture").toString();
			
			// 商品价格
			String pprice = doc.get("product_price").toString();
			
			pro.setPid(pid);
			pro.setName(pname);
			pro.setPicture(ppicture);
			pro.setPrice(pprice);
			
			productList.add(pro);

		}
		result.setProductList(productList);
		
		
		return result;
	}

}
