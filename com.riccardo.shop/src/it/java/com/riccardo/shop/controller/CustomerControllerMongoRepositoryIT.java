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
import com.riccardo.shop.model.Customer;
import com.riccardo.shop.repository.CustomerRepository;
import com.riccardo.shop.repository.PurchaseRepository;
import com.riccardo.shop.repository.RepositoryException;
import com.riccardo.shop.repository.mongo.CustomerMongoRepository;
import com.riccardo.shop.repository.mongo.PurchaseMongoRepository;
import com.riccardo.shop.view.CustomerPurchaseView;

public class CustomerControllerMongoRepositoryIT {

	private static final String CUSTOMER_ID = "C1";
	private static final String CUSTOMER_NAME = "customer1";

	public static final String SHOP_DB_NAME = "shop";
	public static final String CUSTOMER_COLLECTION_NAME = "customer";
	public static final String PURCHASE_COLLECTION_NAME = "purchase";

	@ClassRule
	public static final MongoDBContainer mongo = new MongoDBContainer("mongo:5");

	@Mock
	private CustomerPurchaseView customerPurchaseView;

	private MongoClient client;

	private CustomerRepository customerRepository;
	private PurchaseRepository purchaseRepository;

	private CustomerController customerController;

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
		MongoDatabase database = client.getDatabase(SHOP_DB_NAME);
		database.drop();
		customerRepository = new CustomerMongoRepository(client, SHOP_DB_NAME, CUSTOMER_COLLECTION_NAME);
		purchaseRepository = new PurchaseMongoRepository(client, SHOP_DB_NAME, PURCHASE_COLLECTION_NAME);
		customerController = new CustomerController(customerPurchaseView, customerRepository, purchaseRepository
		);
	}

	@After
	public void tearDown() throws Exception {
		client.close();
		closeable.close();
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