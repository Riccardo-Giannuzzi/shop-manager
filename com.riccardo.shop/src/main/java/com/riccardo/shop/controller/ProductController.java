package com.riccardo.shop.controller;

import com.riccardo.shop.model.Product;
import com.riccardo.shop.repository.ProductRepository;
import com.riccardo.shop.repository.PurchaseRepository;
import com.riccardo.shop.repository.RepositoryException;
import com.riccardo.shop.view.ProductView;

public class ProductController {
	private ProductView productView;
	private ProductRepository productRepository;
	private PurchaseRepository purchaseRepository;

	public ProductController(ProductView productView, ProductRepository productRepository,
			PurchaseRepository purchaseRepository) {
		this.productView = productView;
		this.productRepository = productRepository;
		this.purchaseRepository = purchaseRepository;
	}

	public void allProducts() {
		try {
			productView.showAllProducts(productRepository.findAll());
		} catch (RepositoryException e) {
			handleRepositoryException(e);
		}
	}

	public void newProduct(Product product) {
		try {
			Product existingProduct = productRepository.findById(product.getId());
			if (existingProduct != null) {
				productView.showError("Already existing product with id " + product.getId(),
						existingProduct);
				return;
			}
			productRepository.save(product);
		} catch (RepositoryException e) {
			handleRepositoryException(e);
			return;
		}
		productView.productAdded(product);
	}

	public void deleteProduct(Product product) {
		try {
			if (productRepository.findById(product.getId()) == null) {
				productView.showError("No existing product with id " + product.getId(),
						product);
				return;
			}
			if (!purchaseRepository.findByProductId(product.getId()).isEmpty()) {
				productView.showError("Product with id " + product.getId() + " has purchases",
						product);
				return;
			}
			productRepository.delete(product.getId());
		} catch (RepositoryException e) {
			handleRepositoryException(e);
			return;
		}
		productView.productRemoved(product);
	}

	private void handleRepositoryException(RepositoryException e) {
		productView.showError("Exception occurred in repository: " + e.getMessage());
	}

}
