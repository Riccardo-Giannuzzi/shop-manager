package com.riccardo.shop.controller;

import static org.mockito.Mockito.*;
import static java.util.Arrays.asList;

import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.riccardo.shop.model.Customer;
import com.riccardo.shop.model.Purchase;
import com.riccardo.shop.repository.CustomerRepository;
import com.riccardo.shop.repository.PurchaseRepository;
import com.riccardo.shop.repository.RepositoryException;
import com.riccardo.shop.view.CustomerPurchaseView;

public class CustomerControllerTest {

	private static final String CUSTOMER_ID = "C1";
	private static final String CUSTOMER_NAME = "customer_1";
	private static final String PRODUCT_ID = "P1";
	
	@Mock
	private CustomerPurchaseView customerPurchaseView;

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private PurchaseRepository purchaseRepository;

	@InjectMocks
	private CustomerController customerController;

	private AutoCloseable closeable;

	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
	}

	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}

	@Test
	public void testAllCustomers() throws RepositoryException {
		List<Customer> customers = asList(new Customer());
		when(customerRepository.findAll())
			.thenReturn(customers);
		customerController.allCustomers();
		verify(customerPurchaseView)
			.showAllCustomers(customers);
	}

	@Test
	public void testAllCustomersWhenRepositoryThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		doThrow(new RepositoryException(exceptionMessage))
			.when(customerRepository).findAll();
		customerController.allCustomers();
		verify(customerPurchaseView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testNewCustomerWhenCustomerDoesNotAlreadyExist() throws RepositoryException {
		Customer customerToAdd = new Customer(CUSTOMER_ID, CUSTOMER_NAME);
		when(customerRepository.findById(CUSTOMER_ID))
			.thenReturn(null);
		customerController.newCustomer(customerToAdd);
		InOrder inOrder = inOrder(customerRepository, customerPurchaseView);
		inOrder.verify(customerRepository).save(customerToAdd);
		inOrder.verify(customerPurchaseView).customerAdded(customerToAdd);
	}

	@Test
	public void testNewCustomerWhenCustomerAlreadyExists() throws RepositoryException {
		String existingCustomerName = "existing";
		Customer customerToAdd = new Customer(CUSTOMER_ID, CUSTOMER_NAME);
		Customer existingCustomer = new Customer(CUSTOMER_ID, existingCustomerName);
		when(customerRepository.findById(CUSTOMER_ID))
			.thenReturn(existingCustomer);
		customerController.newCustomer(customerToAdd);
		verify(customerPurchaseView)
			.showError("Already existing customer with id " + CUSTOMER_ID, existingCustomer);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testNewCustomerWhenFindByIdThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Customer customerToAdd = new Customer(CUSTOMER_ID, CUSTOMER_NAME);
		doThrow(new RepositoryException(exceptionMessage))
			.when(customerRepository).findById(CUSTOMER_ID);
		customerController.newCustomer(customerToAdd);
		verify(customerPurchaseView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testNewCustomerWhenSaveThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Customer customerToAdd = new Customer(CUSTOMER_ID, CUSTOMER_NAME);
		when(customerRepository.findById(CUSTOMER_ID))
			.thenReturn(null);
		doThrow(new RepositoryException(exceptionMessage))
			.when(customerRepository).save(customerToAdd);
		customerController.newCustomer(customerToAdd);
		verify(customerPurchaseView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testDeleteCustomerWhenCustomerExistsAndHasNoPurchases() throws RepositoryException {
		Customer customerToDelete = new Customer(CUSTOMER_ID, CUSTOMER_NAME);
		when(customerRepository.findById(CUSTOMER_ID))
			.thenReturn(customerToDelete);
		when(purchaseRepository.findByCustomerId(CUSTOMER_ID))
			.thenReturn(Collections.emptyList());
		customerController.deleteCustomer(customerToDelete);
		InOrder inOrder = inOrder(customerRepository, customerPurchaseView);
		inOrder.verify(customerRepository).delete(CUSTOMER_ID);
		inOrder.verify(customerPurchaseView).customerRemoved(customerToDelete);
	}

	@Test
	public void testDeleteCustomerWhenCustomerDoesNotExist() throws RepositoryException {
		Customer customerToDelete = new Customer(CUSTOMER_ID, CUSTOMER_NAME);
		when(customerRepository.findById(CUSTOMER_ID))
			.thenReturn(null);
		customerController.deleteCustomer(customerToDelete);
		verify(customerPurchaseView)
			.showError("No existing customer with id " + CUSTOMER_ID, customerToDelete);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testDeleteCustomerWhenCustomerHasPurchases() throws RepositoryException {
		Customer customerToDelete = new Customer(CUSTOMER_ID, CUSTOMER_NAME);
		when(customerRepository.findById(CUSTOMER_ID))
			.thenReturn(customerToDelete);
		when(purchaseRepository.findByCustomerId(CUSTOMER_ID))
			.thenReturn(asList(new Purchase(CUSTOMER_ID, PRODUCT_ID)));
		customerController.deleteCustomer(customerToDelete);
		verify(customerPurchaseView)
			.showError("Customer with id " + CUSTOMER_ID + " has purchases", customerToDelete);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testDeleteCustomerWhenFindByIdThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Customer customerToDelete = new Customer(CUSTOMER_ID, CUSTOMER_NAME);
		doThrow(new RepositoryException(exceptionMessage))
			.when(customerRepository).findById(CUSTOMER_ID);
		customerController.deleteCustomer(customerToDelete);
		verify(customerPurchaseView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testDeleteCustomerWhenFindByCustomerIdThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Customer customerToDelete = new Customer(CUSTOMER_ID, CUSTOMER_NAME);
		when(customerRepository.findById(CUSTOMER_ID))
			.thenReturn(customerToDelete);
		doThrow(new RepositoryException(exceptionMessage))
			.when(purchaseRepository).findByCustomerId(CUSTOMER_ID);
		customerController.deleteCustomer(customerToDelete);
		verify(customerPurchaseView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testDeleteCustomerWhenDeleteThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Customer customerToDelete = new Customer(CUSTOMER_ID, CUSTOMER_NAME);
		when(customerRepository.findById(CUSTOMER_ID))
			.thenReturn(customerToDelete);
		when(purchaseRepository.findByCustomerId(CUSTOMER_ID))
			.thenReturn(Collections.emptyList());
		doThrow(new RepositoryException(exceptionMessage))
			.when(customerRepository).delete(CUSTOMER_ID);
		customerController.deleteCustomer(customerToDelete);
		verify(customerPurchaseView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}
}
