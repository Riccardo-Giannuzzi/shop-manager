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

import com.riccardo.shop.model.Customer;
import com.riccardo.shop.model.Product;
import com.riccardo.shop.model.Purchase;
import com.riccardo.shop.repository.CustomerRepository;
import com.riccardo.shop.repository.ProductRepository;
import com.riccardo.shop.repository.PurchaseRepository;
import com.riccardo.shop.repository.RepositoryException;
import com.riccardo.shop.repository.maria.CustomerMariaRepository;
import com.riccardo.shop.repository.maria.ProductMariaRepository;
import com.riccardo.shop.repository.maria.PurchaseMariaRepository;
import com.riccardo.shop.view.CustomerPurchaseView;

public class PurchaseControllerMariaRepositoryIT {

	private static final String MARIADB_IMAGE = "mariadb:11";

	private static final String CUSTOMER_ID = "C1";
	private static final String CUSTOMER_NAME = "customer1";

	private static final String PRODUCT_ID_1 = "P1";
	private static final String PRODUCT_NAME_1 = "product1";
	private static final double PRODUCT_PRICE_1 = 10.0;

	private static final String PRODUCT_ID_2 = "P2";
	private static final String PRODUCT_NAME_2 = "product2";
	private static final double PRODUCT_PRICE_2 = 20.0;

	@ClassRule
	public static final MariaDBContainer<?> mariadb = new MariaDBContainer<>(MARIADB_IMAGE);

	@Mock
	private CustomerPurchaseView customerPurchaseView;

	private static Connection connection;

	private CustomerRepository customerRepository;
	private ProductRepository productRepository;
	private PurchaseRepository purchaseRepository;

	private PurchaseController purchaseController;

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
		customerRepository = new CustomerMariaRepository(connection);
		productRepository = new ProductMariaRepository(connection);
		purchaseRepository = new PurchaseMariaRepository(connection);
		purchaseController = new PurchaseController(customerPurchaseView, purchaseRepository, customerRepository, productRepository);
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
	public void testAllCustomerPurchases() throws RepositoryException {
		Customer customer = new Customer(CUSTOMER_ID, CUSTOMER_NAME);
		Product product = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		Purchase purchase = new Purchase(CUSTOMER_ID, PRODUCT_ID_1);
		customerRepository.save(customer);
		productRepository.save(product);
		purchaseRepository.save(purchase);
		purchaseController.allCustomerPurchases(customer);
		verify(customerPurchaseView).showAllCustomerPurchases(asList(purchase));
	}

	@Test
	public void testAllCustomerAvailableProducts() throws RepositoryException {
		Customer customer = new Customer(CUSTOMER_ID, CUSTOMER_NAME);
		Product product1 = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		Product product2 = new Product(PRODUCT_ID_2, PRODUCT_NAME_2, PRODUCT_PRICE_2);
		Purchase purchase = new Purchase(CUSTOMER_ID, PRODUCT_ID_1);
		customerRepository.save(customer);
		productRepository.save(product1);
		productRepository.save(product2);
		purchaseRepository.save(purchase);
		purchaseController.allCustomerAvailableProducts(customer);
		verify(customerPurchaseView).showAllCustomerAvailableProducts(asList(product2));
	}

	@Test
	public void testNewPurchase() throws RepositoryException {
		Customer customer = new Customer(CUSTOMER_ID, CUSTOMER_NAME);
		Product product = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		Purchase purchase = new Purchase(CUSTOMER_ID, PRODUCT_ID_1);
		customerRepository.save(customer);
		productRepository.save(product);
		purchaseController.newPurchase(purchase);
		verify(customerPurchaseView).purchaseAdded(purchase);
	}

	@Test
	public void testDeletePurchase() throws RepositoryException {
		Customer customer = new Customer(CUSTOMER_ID, CUSTOMER_NAME);
		Product product = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		Purchase purchase = new Purchase(CUSTOMER_ID, PRODUCT_ID_1);
		customerRepository.save(customer);
		productRepository.save(product);
		purchaseRepository.save(purchase);
		purchaseController.deletePurchase(purchase);
		verify(customerPurchaseView).purchaseRemoved(purchase);
	}
}