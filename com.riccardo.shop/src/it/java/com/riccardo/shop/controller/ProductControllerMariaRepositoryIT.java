package com.riccardo.shop.controller;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.containers.MariaDBContainer;

import com.riccardo.shop.model.Product;
import com.riccardo.shop.repository.ProductRepository;
import com.riccardo.shop.repository.PurchaseRepository;
import com.riccardo.shop.repository.RepositoryException;
import com.riccardo.shop.repository.maria.CustomerMariaRepository;
import com.riccardo.shop.repository.maria.ProductMariaRepository;
import com.riccardo.shop.repository.maria.PurchaseMariaRepository;
import com.riccardo.shop.view.ProductView;

public class ProductControllerMariaRepositoryIT {

	private static final String MARIADB_IMAGE = "mariadb:11";

	private static final String PRODUCT_ID = "P1";
	private static final String PRODUCT_NAME = "product1";
	private static final double PRODUCT_PRICE = 10.0;

	@ClassRule
	public static final MariaDBContainer<?> mariadb = new MariaDBContainer<>(MARIADB_IMAGE);

	@Mock
	private ProductView productView;

	private static Connection connection;

	private ProductRepository productRepository;
	private PurchaseRepository purchaseRepository;

	private ProductController productController;

	private AutoCloseable closeable;

	@BeforeClass
	public static void setupConnection() throws SQLException {
		connection = DriverManager.getConnection(mariadb.getJdbcUrl(), mariadb.getUsername(), mariadb.getPassword());
	}

	@Before
	public void setUp() throws SQLException {
		closeable = MockitoAnnotations.openMocks(this);
		try (Statement statement = connection.createStatement()) {
			statement.execute("DROP TABLE IF EXISTS " + PurchaseMariaRepository.PURCHASE_TABLE_NAME);
			statement.execute("DROP TABLE IF EXISTS " + CustomerMariaRepository.CUSTOMER_TABLE_NAME);
			statement.execute("DROP TABLE IF EXISTS " + ProductMariaRepository.PRODUCT_TABLE_NAME);
			statement.execute(
					"CREATE TABLE " + CustomerMariaRepository.CUSTOMER_TABLE_NAME + " ("
					+ CustomerMariaRepository.CUSTOMER_ID_KEY + " VARCHAR(255) PRIMARY KEY, "
					+ CustomerMariaRepository.CUSTOMER_NAME_KEY + " VARCHAR(255) NOT NULL)");
			statement.execute(
					"CREATE TABLE " + ProductMariaRepository.PRODUCT_TABLE_NAME + " ("
					+ ProductMariaRepository.PRODUCT_ID_KEY + " VARCHAR(255) PRIMARY KEY, "
					+ ProductMariaRepository.PRODUCT_NAME_KEY + " VARCHAR(255) NOT NULL, "
					+ ProductMariaRepository.PRODUCT_PRICE_KEY + " DOUBLE NOT NULL)");
			statement.execute(
					"CREATE TABLE " + PurchaseMariaRepository.PURCHASE_TABLE_NAME + " ("
					+ PurchaseMariaRepository.CUSTOMER_ID_KEY + " VARCHAR(255), "
					+ PurchaseMariaRepository.PRODUCT_ID_KEY + " VARCHAR(255), "
					+ "PRIMARY KEY ("
					+ PurchaseMariaRepository.CUSTOMER_ID_KEY + ", "
					+ PurchaseMariaRepository.PRODUCT_ID_KEY + "), "
					+ "FOREIGN KEY ("
					+ PurchaseMariaRepository.CUSTOMER_ID_KEY + ") REFERENCES "
					+ CustomerMariaRepository.CUSTOMER_TABLE_NAME + "("
					+ CustomerMariaRepository.CUSTOMER_ID_KEY + ") "
					+ "ON DELETE RESTRICT, "
					+ "FOREIGN KEY ("
					+ PurchaseMariaRepository.PRODUCT_ID_KEY + ") REFERENCES "
					+ ProductMariaRepository.PRODUCT_TABLE_NAME + "("
					+ ProductMariaRepository.PRODUCT_ID_KEY + ") "
					+ "ON DELETE RESTRICT)");
		}
		productRepository = new ProductMariaRepository(connection);
		purchaseRepository = new PurchaseMariaRepository(connection);
		productController = new ProductController(productView, productRepository, purchaseRepository
		);
	}

	@After
	public void tearDown() throws Exception {
		closeable.close();
	}

	@AfterClass
	public static void tearDownConnection() throws SQLException {
		if (connection != null) {
			connection.close();
		}
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