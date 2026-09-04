package com.riccardo.shop.controller;

import java.util.List;

import com.riccardo.shop.model.Customer;
import com.riccardo.shop.model.Product;
import com.riccardo.shop.model.Purchase;
import com.riccardo.shop.repository.CustomerRepository;
import com.riccardo.shop.repository.ProductRepository;
import com.riccardo.shop.repository.PurchaseRepository;
import com.riccardo.shop.repository.RepositoryException;
import com.riccardo.shop.view.CustomerPurchaseView;

public class PurchaseController {
	private CustomerPurchaseView customerPurchaseView;
	private PurchaseRepository purchaseRepository;
	private CustomerRepository customerRepository;
	private ProductRepository productRepository;

	public PurchaseController(CustomerPurchaseView customerPurchaseView, PurchaseRepository purchaseRepository,
			CustomerRepository customerRepository, ProductRepository productRepository) {
		this.customerPurchaseView = customerPurchaseView;
		this.purchaseRepository = purchaseRepository;
		this.customerRepository = customerRepository;
		this.productRepository = productRepository;
	}

	public void allCustomerPurchases(Customer customer) {
		try {
			customerPurchaseView.showAllCustomerPurchases(purchaseRepository.findByCustomerId(customer.getId()));
		} catch (RepositoryException e) {
			handleRepositoryException(e);
		}
	}

	public void allCustomerAvailableProducts(Customer customer) {
		try {
			List<Product> products = productRepository.findAll();
			List<Purchase> purchases = purchaseRepository.findByCustomerId(customer.getId());
			List<Product> availableProducts = products.stream()
					.filter(product ->purchases.stream()
							.noneMatch(purchase ->purchase.getProductId().equals(product.getId()))).toList();
			customerPurchaseView.showAllCustomerAvailableProducts(availableProducts);
		} catch (RepositoryException e) {
			handleRepositoryException(e);
		}
	}

	public void newPurchase(Purchase purchase) {
		try {
			if (customerRepository.findById(purchase.getCustomerId()) == null) {
				customerPurchaseView.showError(
						"No existing customer with id " + purchase.getCustomerId(),purchase);
				return;
			}
			if (productRepository.findById(purchase.getProductId()) == null) {
				customerPurchaseView.showError(
						"No existing product with id " + purchase.getProductId(),purchase);
				return;
			}
			if (purchaseRepository.findByCustomerIdAndProductId(purchase.getCustomerId(), purchase.getProductId()) != null) {
				customerPurchaseView.showError(
						"Customer " + purchase.getCustomerId() + " already purchased product " + purchase.getProductId(),purchase);
				return;
			}
			purchaseRepository.save(purchase);
		} catch (RepositoryException e) {
			handleRepositoryException(e);
			return;
		}
		customerPurchaseView.purchaseAdded(purchase);
	}

	public void deletePurchase(Purchase purchase) {
		try {
			Purchase existingPurchase = purchaseRepository.findByCustomerIdAndProductId(
					purchase.getCustomerId(), purchase.getProductId());
			if (existingPurchase == null) {
				customerPurchaseView.showError(
						"No existing purchase for customer " + purchase.getCustomerId() + " and product " + purchase.getProductId(), purchase);
				return;
			}
			purchaseRepository.delete(purchase.getCustomerId(), purchase.getProductId());
		} catch (RepositoryException e) {
			handleRepositoryException(e);
			return;
		}
		customerPurchaseView.purchaseRemoved(purchase);
	}

	private void handleRepositoryException(RepositoryException e) {
		customerPurchaseView.showError("Exception occurred in repository: " + e.getMessage());
	}

}
