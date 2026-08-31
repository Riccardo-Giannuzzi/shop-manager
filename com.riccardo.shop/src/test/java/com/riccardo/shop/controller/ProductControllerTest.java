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

import com.riccardo.shop.model.Product;
import com.riccardo.shop.model.Purchase;
import com.riccardo.shop.repository.ProductRepository;
import com.riccardo.shop.repository.PurchaseRepository;
import com.riccardo.shop.repository.RepositoryException;
import com.riccardo.shop.view.ProductView;

public class ProductControllerTest {

	private static final String PRODUCT_ID = "P1";
	private static final String PRODUCT_NAME = "product_1";
	private static final double PRODUCT_PRICE = 10.0;
	private static final String CUSTOMER_ID = "C1";

	@Mock
	private ProductView productView;

	@Mock
	private ProductRepository productRepository;

	@Mock
	private PurchaseRepository purchaseRepository;

	@InjectMocks
	private ProductController productController;

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
	public void testAllProducts() throws RepositoryException {
		List<Product> products = asList(new Product());
		when(productRepository.findAll())
			.thenReturn(products);
		productController.allProducts();
		verify(productView)
			.showAllProducts(products);
	}

	@Test
	public void testAllProductsWhenRepositoryThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		doThrow(new RepositoryException(exceptionMessage))
			.when(productRepository).findAll();
		productController.allProducts();
		verify(productView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(productView));
	}

	@Test
	public void testNewProductWhenProductDoesNotAlreadyExist() throws RepositoryException {
		Product productToAdd = new Product(PRODUCT_ID, PRODUCT_NAME, PRODUCT_PRICE);
		when(productRepository.findById(PRODUCT_ID))
			.thenReturn(null);
		productController.newProduct(productToAdd);
		InOrder inOrder = inOrder(productRepository, productView);
		inOrder.verify(productRepository).save(productToAdd);
		inOrder.verify(productView).productAdded(productToAdd);
	}

	@Test
	public void testNewProductWhenProductAlreadyExists() throws RepositoryException {
		String existingProductName = "existing";
		double existingProductPrice = 20.0;
		Product productToAdd = new Product(PRODUCT_ID, PRODUCT_NAME, PRODUCT_PRICE);
		Product existingProduct = new Product(PRODUCT_ID, existingProductName, existingProductPrice);
		when(productRepository.findById(PRODUCT_ID))
			.thenReturn(existingProduct);
		productController.newProduct(productToAdd);
		verify(productView)
			.showError("Already existing product with id " + PRODUCT_ID, existingProduct);
		verifyNoMoreInteractions(ignoreStubs(productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(productView));
	}

	@Test
	public void testNewProductWhenFindByIdThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Product productToAdd = new Product(PRODUCT_ID, PRODUCT_NAME, PRODUCT_PRICE);
		doThrow(new RepositoryException(exceptionMessage))
			.when(productRepository).findById(PRODUCT_ID);
		productController.newProduct(productToAdd);
		verify(productView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(productView));
	}

	@Test
	public void testNewProductWhenSaveThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Product productToAdd = new Product(PRODUCT_ID, PRODUCT_NAME, PRODUCT_PRICE);
		when(productRepository.findById(PRODUCT_ID))
			.thenReturn(null);
		doThrow(new RepositoryException(exceptionMessage))
			.when(productRepository).save(productToAdd);
		productController.newProduct(productToAdd);
		verify(productView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(productView));
	}

	@Test
	public void testDeleteProductWhenProductExistsAndHasNoPurchases() throws RepositoryException {
		Product productToDelete = new Product(PRODUCT_ID, PRODUCT_NAME, PRODUCT_PRICE);
		when(productRepository.findById(PRODUCT_ID))
			.thenReturn(productToDelete);
		when(purchaseRepository.findByProductId(PRODUCT_ID))
			.thenReturn(Collections.emptyList());
		productController.deleteProduct(productToDelete);
		InOrder inOrder = inOrder(productRepository, productView);
		inOrder.verify(productRepository).delete(PRODUCT_ID);
		inOrder.verify(productView).productRemoved(productToDelete);
	}

	@Test
	public void testDeleteProductWhenProductDoesNotExist() throws RepositoryException {
		Product productToDelete = new Product(PRODUCT_ID, PRODUCT_NAME, PRODUCT_PRICE);
		when(productRepository.findById(PRODUCT_ID))
			.thenReturn(null);
		productController.deleteProduct(productToDelete);
		verify(productView)
			.showError("No existing product with id " + PRODUCT_ID, productToDelete);
		verifyNoMoreInteractions(ignoreStubs(productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(productView));
	}

	@Test
	public void testDeleteProductWhenProductHasPurchases() throws RepositoryException {
		Product productToDelete = new Product(PRODUCT_ID, PRODUCT_NAME, PRODUCT_PRICE);
		when(productRepository.findById(PRODUCT_ID))
			.thenReturn(productToDelete);
		when(purchaseRepository.findByProductId(PRODUCT_ID))
			.thenReturn(asList(new Purchase(CUSTOMER_ID, PRODUCT_ID)));
		productController.deleteProduct(productToDelete);
		verify(productView)
			.showError("Product with id " + PRODUCT_ID + " has purchases", productToDelete);
		verifyNoMoreInteractions(ignoreStubs(productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(productView));
	}

	@Test
	public void testDeleteProductWhenFindByIdThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Product productToDelete = new Product(PRODUCT_ID, PRODUCT_NAME, PRODUCT_PRICE);
		doThrow(new RepositoryException(exceptionMessage))
			.when(productRepository).findById(PRODUCT_ID);
		productController.deleteProduct(productToDelete);
		verify(productView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(productView));
	}

	@Test
	public void testDeleteProductWhenFindByProductIdThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Product productToDelete = new Product(PRODUCT_ID, PRODUCT_NAME, PRODUCT_PRICE);
		when(productRepository.findById(PRODUCT_ID))
			.thenReturn(productToDelete);
		doThrow(new RepositoryException(exceptionMessage))
			.when(purchaseRepository).findByProductId(PRODUCT_ID);
		productController.deleteProduct(productToDelete);
		verify(productView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(productView));
	}

	@Test
	public void testDeleteProductWhenDeleteThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Product productToDelete = new Product(PRODUCT_ID, PRODUCT_NAME, PRODUCT_PRICE);
		when(productRepository.findById(PRODUCT_ID))
			.thenReturn(productToDelete);
		when(purchaseRepository.findByProductId(PRODUCT_ID))
			.thenReturn(Collections.emptyList());
		doThrow(new RepositoryException(exceptionMessage))
			.when(productRepository).delete(PRODUCT_ID);
		productController.deleteProduct(productToDelete);
		verify(productView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(productView));
	}
}
