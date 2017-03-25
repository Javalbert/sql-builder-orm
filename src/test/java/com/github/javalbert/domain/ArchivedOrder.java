/*******************************************************************************
 * Copyright 2017 Albert Shun-Dat Chan
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.github.javalbert.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ArchivedOrder {
	private long archviedOrderId;
	private long orderId;
	private long customerId;
	private long storeKey;
	private String customerName;
	private String storeName;
	private BigDecimal salesAmount;
	private LocalDateTime orderDateTime;
	
	public long getArchviedOrderId() {
		return archviedOrderId;
	}
	public void setArchviedOrderId(long archviedOrderId) {
		this.archviedOrderId = archviedOrderId;
	}
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
	public long getStoreKey() {
		return storeKey;
	}
	public void setStoreKey(long storeKey) {
		this.storeKey = storeKey;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public String getStoreName() {
		return storeName;
	}
	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}
	public BigDecimal getSalesAmount() {
		return salesAmount;
	}
	public void setSalesAmount(BigDecimal salesAmount) {
		this.salesAmount = salesAmount;
	}
	public LocalDateTime getOrderDateTime() {
		return orderDateTime;
	}
	public void setOrderDateTime(LocalDateTime orderDateTime) {
		this.orderDateTime = orderDateTime;
	}
	
	public ArchivedOrder() {}
	
	public ArchivedOrder(long orderId, long customerId, long storeKey) {
		this.customerId = customerId;
		this.orderId = orderId;
		this.storeKey = storeKey;
	}
}
