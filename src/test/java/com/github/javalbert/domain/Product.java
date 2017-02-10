package com.github.javalbert.domain;

import java.math.BigDecimal;

import com.github.javalbert.orm.Column;
import com.github.javalbert.orm.Entity;
import com.github.javalbert.orm.GeneratedValue;
import com.github.javalbert.orm.Id;
import com.github.javalbert.orm.Related;
import com.github.javalbert.orm.Table;
import com.github.javalbert.utils.HashEqualsUtils;

@Entity
@Table(name = "Product")
public class Product {
	@Id
	@GeneratedValue
	@Column("product_id")
	private int productId;
	@Column("order_id")
	private int orderId;
	@Column("product_name")
	private String productName;
	@Column("price")
	private BigDecimal price;
	
	@Related("order")
	private Order order;
	
	public int getProductId() {
		return productId;
	}
	public void setProductId(int productId) {
		this.productId = productId;
	}
	public int getOrderId() {
		return orderId;
	}
	public void setOrderId(int orderId) {
		this.orderId = orderId;
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
	
	public Order getOrder() {
		return order;
	}
	public void setOrder(Order order) {
		this.order = order;
	}
	
	public Product() {}
	
	public Product(int orderId, String productName, BigDecimal price) {
		this.orderId = orderId;
		this.price = price;
		this.productName = productName;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + orderId;
		result = prime * result + HashEqualsUtils.hash(price);
		result = prime * result + productId;
		result = prime * result + ((productName == null) ? 0 : productName.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Product other = (Product) obj;
		if (orderId != other.orderId)
			return false;
		if (price == null) {
			if (other.price != null)
				return false;
		} else if (!HashEqualsUtils.equal(price, other.price))
			return false;
		if (productId != other.productId)
			return false;
		if (productName == null) {
			if (other.productName != null)
				return false;
		} else if (!productName.equals(other.productName))
			return false;
		return true;
	}
}