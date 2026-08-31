package com.riccardo.shop.controller;

import static org.mockito.Mockito.*;
import static java.util.Arrays.asList;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.riccardo.shop.repository.PurchaseRepository;
import com.riccardo.shop.repository.RepositoryException;
import com.riccardo.shop.repository.ProductRepository;
import com.riccardo.shop.model.Customer;
import com.riccardo.shop.model.Product;
import com.riccardo.shop.model.Purchase;
import com.riccardo.shop.repository.CustomerRepository;
import com.riccardo.shop.view.CustomerPurchaseView;

public class PurchaseControllerTest {
	
	private static final String CUSTOMER_ID = "C1";
	private static final String CUSTOMER_NAME = "customer_1";
	private static final String PRODUCT_ID = "P1";
	private static final String PRODUCT_NAME = "product_1";
	private static final double PRODUCT_PRICE = 10.0;

	@Mock
	private CustomerPurchaseView customerPurchaseView;

	@Mock
	private PurchaseRepository purchaseRepository;

	@Mock
	private ProductRepository productRepository;

	@Mock
	private CustomerRepository customerRepository;

	@InjectMocks
	private PurchaseController purchaseController;

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
	public void testAllCustomerPurchases() throws RepositoryException {
		Customer customer = new Customer(CUSTOMER_ID, CUSTOMER_NAME);
		List<Purchase> purchases = asList(new Purchase(CUSTOMER_ID, PRODUCT_ID));
		when(purchaseRepository.findByCustomerId(CUSTOMER_ID))
			.thenReturn(purchases);
		purchaseController.allCustomerPurchases(customer);
		verify(customerPurchaseView)
			.showAllCustomerPurchases(purchases);
	}

	@Test
	public void testAllCustomerPurchasesWhenRepositoryThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Customer customer = new Customer(CUSTOMER_ID, CUSTOMER_NAME);
		doThrow(new RepositoryException(exceptionMessage))
			.when(purchaseRepository).findByCustomerId(CUSTOMER_ID);
		purchaseController.allCustomerPurchases(customer);
		verify(customerPurchaseView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testNewPurchaseWhenPurchaseDoesNotAlreadyExist() throws RepositoryException {
		Purchase purchaseToAdd = new Purchase(CUSTOMER_ID, PRODUCT_ID);
		when(customerRepository.findById(CUSTOMER_ID))
			.thenReturn(new Customer(CUSTOMER_ID, CUSTOMER_NAME));
		when(productRepository.findById(PRODUCT_ID))
			.thenReturn(new Product(PRODUCT_ID, PRODUCT_NAME, PRODUCT_PRICE));
		when(purchaseRepository.findByCustomerIdAndProductId(CUSTOMER_ID, PRODUCT_ID))
			.thenReturn(null);
		purchaseController.newPurchase(purchaseToAdd);
		InOrder inOrder = inOrder(purchaseRepository, customerPurchaseView);
		inOrder.verify(purchaseRepository).save(purchaseToAdd);
		inOrder.verify(customerPurchaseView).purchaseAdded(purchaseToAdd);
	}

	@Test
	public void testNewPurchaseWhenCustomerDoesNotExist() throws RepositoryException {
		Purchase purchaseToAdd = new Purchase(CUSTOMER_ID, PRODUCT_ID);
		when(customerRepository.findById(CUSTOMER_ID))
			.thenReturn(null);
		purchaseController.newPurchase(purchaseToAdd);
		verify(customerPurchaseView)
			.showError("No existing customer with id " + CUSTOMER_ID, purchaseToAdd);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testNewPurchaseWhenProductDoesNotExist() throws RepositoryException {
		Purchase purchaseToAdd = new Purchase(CUSTOMER_ID, PRODUCT_ID);
		when(customerRepository.findById(CUSTOMER_ID))
			.thenReturn(new Customer(CUSTOMER_ID, CUSTOMER_NAME));
		when(productRepository.findById(PRODUCT_ID))
			.thenReturn(null);
		purchaseController.newPurchase(purchaseToAdd);
		verify(customerPurchaseView)
			.showError("No existing product with id " + PRODUCT_ID, purchaseToAdd);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testNewPurchaseWhenPurchaseAlreadyExists() throws RepositoryException {
		Purchase purchaseToAdd = new Purchase(CUSTOMER_ID, PRODUCT_ID);
		when(customerRepository.findById(CUSTOMER_ID))
			.thenReturn(new Customer(CUSTOMER_ID, CUSTOMER_NAME));
		when(productRepository.findById(PRODUCT_ID))
			.thenReturn(new Product(PRODUCT_ID, PRODUCT_NAME, PRODUCT_PRICE));
		when(purchaseRepository.findByCustomerIdAndProductId(CUSTOMER_ID, PRODUCT_ID))
			.thenReturn(purchaseToAdd);
		purchaseController.newPurchase(purchaseToAdd);
		verify(customerPurchaseView)
			.showError(
					"Customer " + CUSTOMER_ID + " already purchased product " + PRODUCT_ID,
					purchaseToAdd);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testNewPurchaseWhenCustomerFindByIdThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Purchase purchaseToAdd = new Purchase(CUSTOMER_ID, PRODUCT_ID);
		doThrow(new RepositoryException(exceptionMessage))
			.when(customerRepository).findById(CUSTOMER_ID);
		purchaseController.newPurchase(purchaseToAdd);
		verify(customerPurchaseView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testNewPurchaseWhenProductFindByIdThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Purchase purchaseToAdd = new Purchase(CUSTOMER_ID, PRODUCT_ID);
		when(customerRepository.findById(CUSTOMER_ID))
			.thenReturn(new Customer(CUSTOMER_ID, CUSTOMER_NAME));
		doThrow(new RepositoryException(exceptionMessage))
			.when(productRepository).findById(PRODUCT_ID);
		purchaseController.newPurchase(purchaseToAdd);
		verify(customerPurchaseView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testNewPurchaseWhenFindExistingPurchaseThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Purchase purchaseToAdd = new Purchase(CUSTOMER_ID, PRODUCT_ID);
		when(customerRepository.findById(CUSTOMER_ID))
			.thenReturn(new Customer(CUSTOMER_ID, CUSTOMER_NAME));
		when(productRepository.findById(PRODUCT_ID))
			.thenReturn(new Product(PRODUCT_ID, PRODUCT_NAME, PRODUCT_PRICE));
		doThrow(new RepositoryException(exceptionMessage))
			.when(purchaseRepository)
			.findByCustomerIdAndProductId(CUSTOMER_ID, PRODUCT_ID);
		purchaseController.newPurchase(purchaseToAdd);
		verify(customerPurchaseView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testNewPurchaseWhenSaveThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Purchase purchaseToAdd = new Purchase(CUSTOMER_ID, PRODUCT_ID);
		when(customerRepository.findById(CUSTOMER_ID))
			.thenReturn(new Customer(CUSTOMER_ID, CUSTOMER_NAME));
		when(productRepository.findById(PRODUCT_ID))
			.thenReturn(new Product(PRODUCT_ID, PRODUCT_NAME, PRODUCT_PRICE));
		when(purchaseRepository.findByCustomerIdAndProductId(CUSTOMER_ID, PRODUCT_ID))
			.thenReturn(null);
		doThrow(new RepositoryException(exceptionMessage))
			.when(purchaseRepository).save(purchaseToAdd);
		purchaseController.newPurchase(purchaseToAdd);
		verify(customerPurchaseView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testDeletePurchaseWhenPurchaseExists() throws RepositoryException {
		Purchase purchaseToDelete = new Purchase(CUSTOMER_ID, PRODUCT_ID);
		when(purchaseRepository.findByCustomerIdAndProductId(CUSTOMER_ID, PRODUCT_ID))
			.thenReturn(purchaseToDelete);
		purchaseController.deletePurchase(purchaseToDelete);
		InOrder inOrder = inOrder(purchaseRepository, customerPurchaseView);
		inOrder.verify(purchaseRepository).delete(CUSTOMER_ID, PRODUCT_ID);
		inOrder.verify(customerPurchaseView).purchaseRemoved(purchaseToDelete);
	}

	@Test
	public void testDeletePurchaseWhenPurchaseDoesNotExist() throws RepositoryException {
		Purchase purchaseToDelete = new Purchase(CUSTOMER_ID, PRODUCT_ID);
		when(purchaseRepository.findByCustomerIdAndProductId(CUSTOMER_ID, PRODUCT_ID))
			.thenReturn(null);
		purchaseController.deletePurchase(purchaseToDelete);
		verify(customerPurchaseView)
			.showError(
					"No existing purchase for customer " + CUSTOMER_ID + " and product " + PRODUCT_ID,
					purchaseToDelete);
		verifyNoMoreInteractions(ignoreStubs(purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testDeletePurchaseWhenFindByCustomerIdAndProductIdThrowsException()
			throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Purchase purchaseToDelete = new Purchase(CUSTOMER_ID, PRODUCT_ID);
		doThrow(new RepositoryException(exceptionMessage))
			.when(purchaseRepository)
			.findByCustomerIdAndProductId(CUSTOMER_ID, PRODUCT_ID);
		purchaseController.deletePurchase(purchaseToDelete);
		verify(customerPurchaseView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testDeletePurchaseWhenDeleteThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Purchase purchaseToDelete = new Purchase(CUSTOMER_ID, PRODUCT_ID);
		when(purchaseRepository.findByCustomerIdAndProductId(CUSTOMER_ID, PRODUCT_ID))
			.thenReturn(purchaseToDelete);
		doThrow(new RepositoryException(exceptionMessage))
			.when(purchaseRepository).delete(CUSTOMER_ID, PRODUCT_ID);
		purchaseController.deletePurchase(purchaseToDelete);
		verify(customerPurchaseView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}
}
