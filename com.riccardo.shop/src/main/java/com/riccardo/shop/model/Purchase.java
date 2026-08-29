package com.riccardo.shop.model;

import java.util.Objects;

public class Purchase {
	private Customer customer;
	private Product product;
	
	public Purchase() {

	}

	public Purchase(Customer customer, Product product) {
		this.customer = customer;
		this.product = product;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	@Override
	public int hashCode() {
		return Objects.hash(customer, product);
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
		return Objects.equals(customer, other.customer) && Objects.equals(product, other.product);
	}

	@Override
	public String toString() {
		return "Purchase [customer=" + customer + ", product=" + product + "]";
	}

}
