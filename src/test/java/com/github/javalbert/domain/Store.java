package com.github.javalbert.domain;

import com.github.javalbert.orm.Column;
import com.github.javalbert.orm.Entity;
import com.github.javalbert.orm.GeneratedValue;
import com.github.javalbert.orm.Id;
import com.github.javalbert.orm.Table;

@Entity
@Table(name = "Store")
public class Store {
	@Id
	@GeneratedValue
	@Column("store_id")
	private int storeId;
	@Column("store_name")
	private String storeName;
	
	public int getStoreId() {
		return storeId;
	}
	public void setStoreId(int storeId) {
		this.storeId = storeId;
	}
	public String getStoreName() {
		return storeName;
	}
	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}
	
	public Store() {}
	
	public Store(String storeName) {
		this.storeName = storeName;
	}
}