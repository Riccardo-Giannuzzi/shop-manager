package com.riccardo.shop.view;

import java.util.List;

import com.riccardo.shop.model.Customer;
import com.riccardo.shop.model.Product;
import com.riccardo.shop.model.Purchase;

public interface CustomerPurchaseView {
	void showAllCustomers(List<Customer> customers);

	void showAllCustomerPurchases(List<Purchase> purchases);
	
	void showAllCustomerAvailableProducts(List<Product> products);

	void showError(String message);

	void showError(String message, Customer customer);

	void showError(String message, Purchase purchase);

	void customerAdded(Customer customer);

	void customerRemoved(Customer customer);

	void purchaseAdded(Purchase purchase);

	void purchaseRemoved(Purchase purchase);
}
