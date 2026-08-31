package com.riccardo.shop.model;

import java.util.Objects;

public class Purchase {
	private String customerId;
	private String productId;
	
	public Purchase() {

	}

	public Purchase(String customerId, String productId) {
		this.customerId = customerId;
		this.productId = productId;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(customerId, productId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Purchase other = (Purchase) obj;
		return Objects.equals(customerId, other.customerId) && Objects.equals(productId, other.productId);
	}

	@Override
	public String toString() {
		return "Purchase [customerId=" + customerId + ", productId=" + productId + "]";
	}

}
