package com.riccardo.shop.controller;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.containers.MongoDBContainer;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import com.riccardo.shop.model.Product;
import com.riccardo.shop.repository.ProductRepository;
import com.riccardo.shop.repository.PurchaseRepository;
import com.riccardo.shop.repository.RepositoryException;
import com.riccardo.shop.repository.mongo.ProductMongoRepository;
import com.riccardo.shop.repository.mongo.PurchaseMongoRepository;
import com.riccardo.shop.view.ProductView;

public class ProductControllerMongoRepositoryIT {

	private static final String PRODUCT_ID = "P1";
	private static final String PRODUCT_NAME = "product1";
	private static final double PRODUCT_PRICE = 10.0;

	@ClassRule
	public static final MongoDBContainer mongo = new MongoDBContainer("mongo:5");

	@Mock
	private ProductView productView;

	private MongoClient client;

	private ProductRepository productRepository;
	private PurchaseRepository purchaseRepository;

	private ProductController productController;

	private AutoCloseable closeable;

	@Before
	public void setUp() {
		closeable = MockitoAnnotations.openMocks(this);
		client = new MongoClient(
				new ServerAddress(
						mongo.getHost(),
						mongo.getFirstMappedPort()
				)
		);
		MongoDatabase database = client.getDatabase(ProductMongoRepository.SHOP_DB_NAME);
		database.drop();
		productRepository = new ProductMongoRepository(client);
		purchaseRepository = new PurchaseMongoRepository(client);
		productController = new ProductController(productView, productRepository, purchaseRepository
		);
	}

	@After
	public void tearDown() throws Exception {
		client.close();
		closeable.close();
	}

	@Test
	public void testAllProducts() throws RepositoryException {
		Product product = new Product(PRODUCT_ID, PRODUCT_NAME, PRODUCT_PRICE);
		productRepository.save(product);
		productController.allProducts();
		verify(productView).showAllProducts(asList(product));
	}

	@Test
	public void testNewProduct() {
		Product product = new Product(PRODUCT_ID, PRODUCT_NAME, PRODUCT_PRICE);
		productController.newProduct(product);
		verify(productView).productAdded(product);
	}

	@Test
	public void testDeleteProduct() throws RepositoryException {
		Product product = new Product(PRODUCT_ID, PRODUCT_NAME, PRODUCT_PRICE);
		productRepository.save(product);
		productController.deleteProduct(product);
		verify(productView).productRemoved(product);
	}
}
