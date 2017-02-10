package com.github.javalbert.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.javalbert.orm.Column;
import com.github.javalbert.orm.Entity;
import com.github.javalbert.orm.GeneratedValue;
import com.github.javalbert.orm.Id;
import com.github.javalbert.orm.Related;
import com.github.javalbert.orm.Table;

@Entity
@Table(name = "Orders")
public class Order {
	@Id
	@GeneratedValue
	@Column("order_id")
	private int orderId;
	@Column("customer_id")
	private int customerId;
	@Column("store_id")
	private int storeId;
	@Column("sales_amount")
	private BigDecimal salesAmount;
	@Column("order_datetime")
	private Date orderDatetime;
	
	@Related("productList")
	private List<Product> productList;
	@Related("productSet")
	private Set<Product> productSet;
	@Related("productMap")
	private Map<Integer, Product> productMap;
	
	public int getOrderId() {
		return orderId;
	}
	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}
	public int getCustomerId() {
		return customerId;
	}
	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}
	public int getStoreId() {
		return storeId;
	}
	public void setStoreId(int storeId) {
		this.storeId = storeId;
	}
	public BigDecimal getSalesAmount() {
		return salesAmount;
	}
	public void setSalesAmount(BigDecimal salesAmount) {
		this.salesAmount = salesAmount;
	}
	public Date getOrderDatetime() {
		return orderDatetime;
	}
	public void setOrderDatetime(Date orderDatetime) {
		this.orderDatetime = orderDatetime;
	}
	
	public List<Product> getProductList() {
		return productList;
	}
	public void setProductList(List<Product> productList) {
		this.productList = productList;
	}
	public Set<Product> getProductSet() {
		return productSet;
	}
	public void setProductSet(Set<Product> productSet) {
		this.productSet = productSet;
	}
	public Map<Integer, Product> getProductMap() {
		return productMap;
	}
	public void setProductMap(Map<Integer, Product> productMap) {
		this.productMap = productMap;
	}
	
	public Order() {}
	
	public Order(int customerId, int storeId, BigDecimal salesAmount, Date orderDatetime) {
		this.customerId = customerId;
		this.orderDatetime = orderDatetime;
		this.salesAmount = salesAmount;
		this.storeId = storeId;
	}
}