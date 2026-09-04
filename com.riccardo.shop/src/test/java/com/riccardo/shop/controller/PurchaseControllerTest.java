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
	

	private static final String CUSTOMER_ID_1 = "C1";
	private static final String CUSTOMER_NAME_1 = "customer_1";

	private static final String PRODUCT_ID_1 = "P1";
	private static final String PRODUCT_NAME_1 = "product_1";
	private static final double PRODUCT_PRICE_1 = 10.0;

	private static final String PRODUCT_ID_2 = "P2";
	private static final String PRODUCT_NAME_2 = "product_2";
	private static final double PRODUCT_PRICE_2 = 20.0;

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
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		List<Purchase> purchases = asList(new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1));
		when(purchaseRepository.findByCustomerId(CUSTOMER_ID_1))
			.thenReturn(purchases);
		purchaseController.allCustomerPurchases(customer);
		verify(customerPurchaseView)
			.showAllCustomerPurchases(purchases);
	}

	@Test
	public void testAllCustomerPurchasesWhenRepositoryThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		doThrow(new RepositoryException(exceptionMessage))
			.when(purchaseRepository).findByCustomerId(CUSTOMER_ID_1);
		purchaseController.allCustomerPurchases(customer);
		verify(customerPurchaseView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testAllCustomerAvailableProductsWhenAllProductsWerePurchased() throws RepositoryException {
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		Product product1 = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		Product product2 = new Product(PRODUCT_ID_2, PRODUCT_NAME_2, PRODUCT_PRICE_2);
		when(productRepository.findAll())
			.thenReturn(asList(product1, product2));
		when(purchaseRepository.findByCustomerId(CUSTOMER_ID_1))
			.thenReturn(asList(new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1), new Purchase(CUSTOMER_ID_1, PRODUCT_ID_2)));
		purchaseController.allCustomerAvailableProducts(customer);
		verify(customerPurchaseView)
			.showAllCustomerAvailableProducts(asList());
	}

	@Test
	public void testAllCustomerAvailableProductsWhenCustomerHasNoPurchases() throws RepositoryException {
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		Product product1 = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		Product product2 = new Product(PRODUCT_ID_2, PRODUCT_NAME_2, PRODUCT_PRICE_2);
		when(productRepository.findAll())
			.thenReturn(asList(product1, product2));
		when(purchaseRepository.findByCustomerId(CUSTOMER_ID_1))
			.thenReturn(asList());
		purchaseController.allCustomerAvailableProducts(customer);
		verify(customerPurchaseView).showAllCustomerAvailableProducts(asList(product1, product2));
	}

	@Test
	public void testAllCustomerAvailableProductsShouldExcludeAlreadyPurchasedProducts() throws RepositoryException {
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		Product product1 = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		Product product2 = new Product(PRODUCT_ID_2, PRODUCT_NAME_2, PRODUCT_PRICE_2);
		when(productRepository.findAll())
			.thenReturn(asList(product1, product2));
		when(purchaseRepository.findByCustomerId(CUSTOMER_ID_1))
			.thenReturn(asList(new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1)));
		purchaseController.allCustomerAvailableProducts(customer);
		verify(customerPurchaseView).showAllCustomerAvailableProducts(asList(product2));
	}

	@Test
	public void testAllCustomerAvailableProductsWhenFindAllThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		doThrow(new RepositoryException(exceptionMessage))
			.when(productRepository).findAll();
		purchaseController.allCustomerAvailableProducts(customer);
		verify(customerPurchaseView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testAllCustomerAvailableProductsWhenFindByCustomerIdThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		when(productRepository.findAll())
			.thenReturn(asList(new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1)));
		doThrow(new RepositoryException(exceptionMessage))
			.when(purchaseRepository).findByCustomerId(CUSTOMER_ID_1);
		purchaseController.allCustomerAvailableProducts(customer);
		verify(customerPurchaseView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testNewPurchaseWhenPurchaseDoesNotAlreadyExist() throws RepositoryException {
		Purchase purchaseToAdd = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		when(customerRepository.findById(CUSTOMER_ID_1))
			.thenReturn(new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1));
		when(productRepository.findById(PRODUCT_ID_1))
			.thenReturn(new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1));
		when(purchaseRepository.findByCustomerIdAndProductId(CUSTOMER_ID_1, PRODUCT_ID_1))
			.thenReturn(null);
		purchaseController.newPurchase(purchaseToAdd);
		InOrder inOrder = inOrder(purchaseRepository, customerPurchaseView);
		inOrder.verify(purchaseRepository).save(purchaseToAdd);
		inOrder.verify(customerPurchaseView).purchaseAdded(purchaseToAdd);
	}

	@Test
	public void testNewPurchaseWhenCustomerDoesNotExist() throws RepositoryException {
		Purchase purchaseToAdd = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		when(customerRepository.findById(CUSTOMER_ID_1))
			.thenReturn(null);
		purchaseController.newPurchase(purchaseToAdd);
		verify(customerPurchaseView)
			.showError("No existing customer with id " + CUSTOMER_ID_1, purchaseToAdd);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testNewPurchaseWhenProductDoesNotExist() throws RepositoryException {
		Purchase purchaseToAdd = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		when(customerRepository.findById(CUSTOMER_ID_1))
			.thenReturn(new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1));
		when(productRepository.findById(PRODUCT_ID_1))
			.thenReturn(null);
		purchaseController.newPurchase(purchaseToAdd);
		verify(customerPurchaseView)
			.showError("No existing product with id " + PRODUCT_ID_1, purchaseToAdd);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testNewPurchaseWhenPurchaseAlreadyExists() throws RepositoryException {
		Purchase purchaseToAdd = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		when(customerRepository.findById(CUSTOMER_ID_1))
			.thenReturn(new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1));
		when(productRepository.findById(PRODUCT_ID_1))
			.thenReturn(new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1));
		when(purchaseRepository.findByCustomerIdAndProductId(CUSTOMER_ID_1, PRODUCT_ID_1))
			.thenReturn(purchaseToAdd);
		purchaseController.newPurchase(purchaseToAdd);
		verify(customerPurchaseView)
			.showError("Customer " + CUSTOMER_ID_1 + " already purchased product " + PRODUCT_ID_1,purchaseToAdd);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testNewPurchaseWhenCustomerFindByIdThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Purchase purchaseToAdd = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		doThrow(new RepositoryException(exceptionMessage))
			.when(customerRepository).findById(CUSTOMER_ID_1);
		purchaseController.newPurchase(purchaseToAdd);
		verify(customerPurchaseView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testNewPurchaseWhenProductFindByIdThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Purchase purchaseToAdd = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		when(customerRepository.findById(CUSTOMER_ID_1))
			.thenReturn(new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1));
		doThrow(new RepositoryException(exceptionMessage))
			.when(productRepository).findById(PRODUCT_ID_1);
		purchaseController.newPurchase(purchaseToAdd);
		verify(customerPurchaseView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testNewPurchaseWhenFindExistingPurchaseThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Purchase purchaseToAdd = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		when(customerRepository.findById(CUSTOMER_ID_1))
			.thenReturn(new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1));
		when(productRepository.findById(PRODUCT_ID_1))
			.thenReturn(new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1));
		doThrow(new RepositoryException(exceptionMessage))
			.when(purchaseRepository)
			.findByCustomerIdAndProductId(CUSTOMER_ID_1, PRODUCT_ID_1);
		purchaseController.newPurchase(purchaseToAdd);
		verify(customerPurchaseView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(customerRepository, productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testNewPurchaseWhenSaveThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Purchase purchaseToAdd = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		when(customerRepository.findById(CUSTOMER_ID_1))
			.thenReturn(new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1));
		when(productRepository.findById(PRODUCT_ID_1))
			.thenReturn(new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1));
		when(purchaseRepository.findByCustomerIdAndProductId(CUSTOMER_ID_1, PRODUCT_ID_1))
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
		Purchase purchaseToDelete = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		when(purchaseRepository.findByCustomerIdAndProductId(CUSTOMER_ID_1, PRODUCT_ID_1))
			.thenReturn(purchaseToDelete);
		purchaseController.deletePurchase(purchaseToDelete);
		InOrder inOrder = inOrder(purchaseRepository, customerPurchaseView);
		inOrder.verify(purchaseRepository).delete(CUSTOMER_ID_1, PRODUCT_ID_1);
		inOrder.verify(customerPurchaseView).purchaseRemoved(purchaseToDelete);
	}

	@Test
	public void testDeletePurchaseWhenPurchaseDoesNotExist() throws RepositoryException {
		Purchase purchaseToDelete = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		when(purchaseRepository.findByCustomerIdAndProductId(CUSTOMER_ID_1, PRODUCT_ID_1))
			.thenReturn(null);
		purchaseController.deletePurchase(purchaseToDelete);
		verify(customerPurchaseView)
			.showError(
					"No existing purchase for customer " + CUSTOMER_ID_1 + " and product " + PRODUCT_ID_1,
					purchaseToDelete);
		verifyNoMoreInteractions(ignoreStubs(purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testDeletePurchaseWhenFindByCustomerIdAndProductIdThrowsException()
			throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Purchase purchaseToDelete = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		doThrow(new RepositoryException(exceptionMessage))
			.when(purchaseRepository)
			.findByCustomerIdAndProductId(CUSTOMER_ID_1, PRODUCT_ID_1);
		purchaseController.deletePurchase(purchaseToDelete);
		verify(customerPurchaseView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}

	@Test
	public void testDeletePurchaseWhenDeleteThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Purchase purchaseToDelete = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		when(purchaseRepository.findByCustomerIdAndProductId(CUSTOMER_ID_1, PRODUCT_ID_1))
			.thenReturn(purchaseToDelete);
		doThrow(new RepositoryException(exceptionMessage))
			.when(purchaseRepository).delete(CUSTOMER_ID_1, PRODUCT_ID_1);
		purchaseController.deletePurchase(purchaseToDelete);
		verify(customerPurchaseView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(customerPurchaseView));
	}
}
