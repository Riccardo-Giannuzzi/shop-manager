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
		verifyNoMoreInteractions(ignoreStubs(productRepository));
		verifyNoMoreInteractions(ignoreStubs(productView));
	}

	@Test
	public void testNewProductWhenProductDoesNotAlreadyExist() throws RepositoryException {
		Product product = new Product("1", "test", 10.0);
		when(productRepository.findById("1"))
			.thenReturn(null);
		productController.newProduct(product);
		InOrder inOrder = inOrder(productRepository, productView);
		inOrder.verify(productRepository).save(product);
		inOrder.verify(productView).productAdded(product);
	}

	@Test
	public void testNewProductWhenProductAlreadyExists() throws RepositoryException {
		Product productToAdd = new Product("1", "test", 10.0);
		Product existingProduct = new Product("1", "existing", 20.0);
		when(productRepository.findById("1"))
			.thenReturn(existingProduct);
		productController.newProduct(productToAdd);
		verify(productView)
			.showError("Already existing product with id 1", existingProduct);
		verifyNoMoreInteractions(ignoreStubs(productRepository));
		verifyNoMoreInteractions(ignoreStubs(productView));
	}

	@Test
	public void testNewProductWhenFindByIdThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Product product = new Product("1", "test", 10.0);
		doThrow(new RepositoryException(exceptionMessage))
			.when(productRepository).findById("1");
		productController.newProduct(product);
		verify(productView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(productView));
	}

	@Test
	public void testNewProductWhenSaveThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Product product = new Product("1", "test", 10.0);
		when(productRepository.findById("1"))
			.thenReturn(null);
		doThrow(new RepositoryException(exceptionMessage))
			.when(productRepository).save(product);
		productController.newProduct(product);
		verify(productView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(productView));
	}

	@Test
	public void testDeleteProductWhenProductExistsAndHasNoPurchases() throws RepositoryException {
		Product productToDelete = new Product("1", "test", 10.0);
		when(productRepository.findById("1"))
			.thenReturn(productToDelete);
		when(purchaseRepository.findByProductId("1"))
			.thenReturn(Collections.emptyList());
		productController.deleteProduct(productToDelete);
		InOrder inOrder = inOrder(productRepository, productView);
		inOrder.verify(productRepository).delete("1");
		inOrder.verify(productView).productRemoved(productToDelete);
	}

	@Test
	public void testDeleteProductWhenProductDoesNotExist() throws RepositoryException {
		Product product = new Product("1", "test", 10.0);
		when(productRepository.findById("1"))
			.thenReturn(null);
		productController.deleteProduct(product);
		verify(productView)
			.showError("No existing product with id 1", product);
		verifyNoMoreInteractions(ignoreStubs(productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(productView));
	}

	@Test
	public void testDeleteProductWhenProductHasPurchases() throws RepositoryException {
		Product product = new Product("1", "test", 10.0);
		when(productRepository.findById("1"))
			.thenReturn(product);
		when(purchaseRepository.findByProductId("1"))
			.thenReturn(asList(new Purchase("1", "1")));
		productController.deleteProduct(product);
		verify(productView)
			.showError("Product with id 1 has purchases", product);
		verifyNoMoreInteractions(ignoreStubs(productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(productView));
	}

	@Test
	public void testDeleteProductWhenFindByIdThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Product product = new Product("1", "test", 10.0);
		doThrow(new RepositoryException(exceptionMessage))
			.when(productRepository).findById("1");
		productController.deleteProduct(product);
		verify(productView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(productView));
	}

	@Test
	public void testDeleteProductWhenFindByProductIdThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Product product = new Product("1", "test", 10.0);
		when(productRepository.findById("1"))
			.thenReturn(product);
		doThrow(new RepositoryException(exceptionMessage))
			.when(purchaseRepository).findByProductId("1");
		productController.deleteProduct(product);
		verify(productView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(productView));
	}

	@Test
	public void testDeleteProductWhenDeleteThrowsException() throws RepositoryException {
		String exceptionMessage = "Database connection failed";
		Product product = new Product("1", "test", 10.0);
		when(productRepository.findById("1"))
			.thenReturn(product);
		when(purchaseRepository.findByProductId("1"))
			.thenReturn(Collections.emptyList());
		doThrow(new RepositoryException(exceptionMessage))
			.when(productRepository).delete("1");
		productController.deleteProduct(product);
		verify(productView)
			.showError("Exception occurred in repository: " + exceptionMessage);
		verifyNoMoreInteractions(ignoreStubs(productRepository, purchaseRepository));
		verifyNoMoreInteractions(ignoreStubs(productView));
	}
}
