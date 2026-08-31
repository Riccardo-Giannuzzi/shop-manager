package com.riccardo.shop.view;

import java.util.List;

import com.riccardo.shop.model.Product;

public interface ProductView {
	void showAllProducts(List<Product> products);

	void showError(String message);

	void showError(String message, Product product);

	void productAdded(Product product);

	void productRemoved(Product product);
}
