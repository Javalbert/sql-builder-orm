package com.github.javalbert.domain;

import java.util.List;

import com.github.javalbert.orm.Column;
import com.github.javalbert.orm.Entity;
import com.github.javalbert.orm.GeneratedValue;
import com.github.javalbert.orm.Id;
import com.github.javalbert.orm.Related;
import com.github.javalbert.orm.Table;

@Entity
@Table(name = "Customer")
public class Customer {
	@Id
	@GeneratedValue
	@Column("customer_id")
	private int customerId;
	@Column("full_name")
	private String fullName;
	
	@Related("orderList")
	private List<Order> orders;
	
	public int getCustomerId() {
		return customerId;
	}
	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	public List<Order> getOrders() {
		return orders;
	}
	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}
	
	public Customer() {}
	
	public Customer(String fullName) {
		this.fullName = fullName;
	}
}