package com.github.javalbert.domain;

import java.math.BigDecimal;

import com.github.javalbert.orm.Column;
import com.github.javalbert.orm.Entity;
import com.github.javalbert.orm.GeneratedValue;
import com.github.javalbert.orm.Id;
import com.github.javalbert.orm.Table;

@Entity
@Table(name = "Product")
public class Product {
	@Id
	@GeneratedValue
	@Column("product_id")
	private int productId;
	@Column("product_name")
	private String productName;
	@Column("price")
	private BigDecimal price;
	
	public int getProductId() {
		return productId;
	}
	public void setProductId(int productId) {
		this.productId = productId;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
}