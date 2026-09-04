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
import com.riccardo.shop.repository.CustomerRepository;
import com.riccardo.shop.repository.PurchaseRepository;
import com.riccardo.shop.repository.RepositoryException;
import com.riccardo.shop.repository.maria.CustomerMariaRepository;
import com.riccardo.shop.repository.maria.ProductMariaRepository;
import com.riccardo.shop.repository.maria.PurchaseMariaRepository;
import com.riccardo.shop.view.CustomerPurchaseView;

public class CustomerControllerMariaRepositoryIT {

	private static final String MARIADB_IMAGE = "mariadb:11";

	private static final String CUSTOMER_ID = "C1";
	private static final String CUSTOMER_NAME = "customer1";

	@ClassRule
	public static final MariaDBContainer<?> mariadb = new MariaDBContainer<>(MARIADB_IMAGE);

	@Mock
	private CustomerPurchaseView customerPurchaseView;

	private static Connection connection;

	private CustomerRepository customerRepository;
	private PurchaseRepository purchaseRepository;

	private CustomerController customerController;

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
		purchaseRepository = new PurchaseMariaRepository(connection);
		customerController = new CustomerController(customerPurchaseView, customerRepository, purchaseRepository);
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
	public void testAllCustomers() throws RepositoryException {
		Customer customer = new Customer(CUSTOMER_ID, CUSTOMER_NAME);
		customerRepository.save(customer);
		customerController.allCustomers();
		verify(customerPurchaseView).showAllCustomers(asList(customer));
	}

	@Test
	public void testNewCustomer() {
		Customer customer = new Customer(CUSTOMER_ID, CUSTOMER_NAME);
		customerController.newCustomer(customer);
		verify(customerPurchaseView).customerAdded(customer);
	}

	@Test
	public void testDeleteCustomer() throws RepositoryException {
		Customer customer = new Customer(CUSTOMER_ID, CUSTOMER_NAME);
		customerRepository.save(customer);
		customerController.deleteCustomer(customer);
		verify(customerPurchaseView).customerRemoved(customer);
	}
}