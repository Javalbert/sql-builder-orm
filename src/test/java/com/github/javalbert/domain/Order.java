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
	private long orderId;
	@Column("customer_id")
	private long customerId;
	@Column("store_id")
	private Long storeId;
	@Column("sales_amount")
	private BigDecimal salesAmount;
	@Column("order_datetime")
	private Date orderDatetime;
	
	@Related("store")
	private Store store;
	
	@Related("productList")
	private List<Product> productList;
	@Related("productSet")
	private Set<Product> productSet;
	@Related("productMap")
	private Map<Long, Product> productMap;
	
	public long getOrderId() {
		return orderId;
	}
	public void setOrderId(long orderId) {
		this.orderId = orderId;
	}
	public long getCustomerId() {
		return customerId;
	}
	public void setCustomerId(long customerId) {
		this.customerId = customerId;
	}
	public Long getStoreId() {
		return storeId;
	}
	public void setStoreId(Long storeId) {
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
	
	public Store getStore() {
		return store;
	}
	public void setStore(Store store) {
		this.store = store;
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
	public Map<Long, Product> getProductMap() {
		return productMap;
	}
	public void setProductMap(Map<Long, Product> productMap) {
		this.productMap = productMap;
	}
	
	public Order() {}
	
	public Order(long customerId, Long storeId) {
		this(customerId, storeId, null, null);
	}
	
	public Order(long customerId, Long storeId, BigDecimal salesAmount, Date orderDatetime) {
		this.customerId = customerId;
		this.orderDatetime = orderDatetime;
		this.salesAmount = salesAmount;
		this.storeId = storeId;
	}
}