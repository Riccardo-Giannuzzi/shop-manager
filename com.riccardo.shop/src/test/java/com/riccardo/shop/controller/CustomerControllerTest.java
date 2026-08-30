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
		Customer customer = new Customer("1", "test");
		when(customerRepository.findById("1"))
			.thenReturn(null);
		customerController.newCustomer(customer);
		InOrder inOrder = inOrder(customerRepository, customerPurchaseView);
		inOrder.verify(customerRepository).save(customer);
		inOrder.verify(customerPurchaseView).customerAdded(customer);
	}

	@Test
	public void testNewCustomerWhenCustomerAlreadyExists() throws RepositoryException {
		Customer customerToAdd = new Customer("1", "test");
		Customer existingCustomer = new Customer("1", "existing");
		when(customerRepository.findById("1"))
			.thenReturn(existingCustomer);
		customerController.newCustomer(customerToAdd);
		verify(customerPurchaseView)
			.showError("Already existing customer with id 1", existingCustomer);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testNewCustomerWhenFindByIdThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Customer customer = new Customer("1", "test");
		doThrow(new RepositoryException(exceptionMessage))
			.when(customerRepository).findById("1");
		customerController.newCustomer(customer);
		verify(customerPurchaseView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testNewCustomerWhenSaveThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Customer customer = new Customer("1", "test");
		when(customerRepository.findById("1"))
			.thenReturn(null);
		doThrow(new RepositoryException(exceptionMessage))
			.when(customerRepository).save(customer);
		customerController.newCustomer(customer);
		verify(customerPurchaseView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testDeleteCustomerWhenCustomerExistsAndHasNoPurchases() throws RepositoryException {
		Customer customerToDelete = new Customer("1", "test");
		when(customerRepository.findById("1"))
			.thenReturn(customerToDelete);
		when(purchaseRepository.findByCustomerId("1"))
			.thenReturn(Collections.emptyList());
		customerController.deleteCustomer(customerToDelete);
		InOrder inOrder = inOrder(customerRepository, customerPurchaseView);
		inOrder.verify(customerRepository).delete("1");
		inOrder.verify(customerPurchaseView).customerRemoved(customerToDelete);
	}

	@Test
	public void testDeleteCustomerWhenCustomerDoesNotExist() throws RepositoryException {
		Customer customer = new Customer("1", "test");
		when(customerRepository.findById("1"))
			.thenReturn(null);
		customerController.deleteCustomer(customer);
		verify(customerPurchaseView)
			.showError("No existing customer with id 1", customer);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testDeleteCustomerWhenCustomerHasPurchases() throws RepositoryException {
		Customer customer = new Customer("1", "test");
		when(customerRepository.findById("1"))
			.thenReturn(customer);
		when(purchaseRepository.findByCustomerId("1"))
			.thenReturn(asList(new Purchase("1", "1")));
		customerController.deleteCustomer(customer);
		verify(customerPurchaseView)
			.showError("Customer with id 1 has purchases", customer);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testDeleteCustomerWhenFindByIdThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Customer customer = new Customer("1", "test");
		doThrow(new RepositoryException(exceptionMessage))
			.when(customerRepository).findById("1");
		customerController.deleteCustomer(customer);
		verify(customerPurchaseView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testDeleteCustomerWhenFindByCustomerIdThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Customer customer = new Customer("1", "test");
		when(customerRepository.findById("1"))
			.thenReturn(customer);
		doThrow(new RepositoryException(exceptionMessage))
			.when(purchaseRepository).findByCustomerId("1");
		customerController.deleteCustomer(customer);
		verify(customerPurchaseView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testDeleteCustomerWhenDeleteThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Customer customer = new Customer("1", "test");
		when(customerRepository.findById("1"))
			.thenReturn(customer);
		when(purchaseRepository.findByCustomerId("1"))
			.thenReturn(Collections.emptyList());
		doThrow(new RepositoryException(exceptionMessage))
			.when(customerRepository).delete("1");
		customerController.deleteCustomer(customer);
		verify(customerPurchaseView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}
}
