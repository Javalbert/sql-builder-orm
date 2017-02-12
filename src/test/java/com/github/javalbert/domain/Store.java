package com.github.javalbert.domain;

import java.util.Collection;

import com.github.javalbert.orm.Column;
import com.github.javalbert.orm.Entity;
import com.github.javalbert.orm.GeneratedValue;
import com.github.javalbert.orm.Id;
import com.github.javalbert.orm.Related;
import com.github.javalbert.orm.Table;

@Entity
@Table(name = "Store")
public class Store {
	@Id
	@GeneratedValue
	@Column("store_key")
	private long storeKey;
	@Column("store_name")
	private String storeName;
	
	@Related("orders")
	private Collection<Order> orders;
	
	public long getStoreKey() {
		return storeKey;
	}
	public void setStoreKey(long storeKey) {
		this.storeKey = storeKey;
	}
	public String getStoreName() {
		return storeName;
	}
	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}
	
	public Collection<Order> getOrders() {
		return orders;
	}
	public void setOrders(Collection<Order> orders) {
		this.orders = orders;
	}
	
	public Store() {}
	
	public Store(String storeName) {
		this.storeName = storeName;
	}
}