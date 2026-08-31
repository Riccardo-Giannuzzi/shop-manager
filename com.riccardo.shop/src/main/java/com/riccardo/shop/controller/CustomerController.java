package com.riccardo.shop.controller;

import com.riccardo.shop.model.Customer;
import com.riccardo.shop.repository.CustomerRepository;
import com.riccardo.shop.repository.PurchaseRepository;
import com.riccardo.shop.repository.RepositoryException;
import com.riccardo.shop.view.CustomerPurchaseView;

public class CustomerController {
	private CustomerPurchaseView customerPurchaseView;
	private CustomerRepository customerRepository;
	private PurchaseRepository purchaseRepository;

	public CustomerController(CustomerPurchaseView customerPurchaseView, CustomerRepository customerRepository,
			PurchaseRepository purchaseRepository) {
		this.customerPurchaseView = customerPurchaseView;
		this.customerRepository = customerRepository;
		this.purchaseRepository = purchaseRepository;
	}

	public void allCustomers() {
		try {
			customerPurchaseView.showAllCustomers(customerRepository.findAll());
		} catch (RepositoryException e) {
			handleRepositoryException(e);
		}
	}

	public void newCustomer(Customer customer) {
		try {
			Customer existingCustomer = customerRepository.findById(customer.getId());
			if (existingCustomer != null) {
				customerPurchaseView.showError("Already existing customer with id " + customer.getId(),
						existingCustomer);
				return;
			}
			customerRepository.save(customer);
		} catch (RepositoryException e) {
			handleRepositoryException(e);
			return;
		}
		customerPurchaseView.customerAdded(customer);
	}

	public void deleteCustomer(Customer customer) {
		try {
			if (customerRepository.findById(customer.getId()) == null) {
				customerPurchaseView.showError("No existing customer with id " + customer.getId(),
						customer);
				return;
			}
			if (!purchaseRepository.findByCustomerId(customer.getId()).isEmpty()) {
				customerPurchaseView.showError("Customer with id " + customer.getId() + " has purchases",
						customer);
				return;
			}
			customerRepository.delete(customer.getId());
		} catch (RepositoryException e) {
			handleRepositoryException(e);
			return;
		}
		customerPurchaseView.customerRemoved(customer);
	}

	private void handleRepositoryException(RepositoryException e) {
		customerPurchaseView.showError("Exception occurred in repository: " + e.getMessage());
	}

}
