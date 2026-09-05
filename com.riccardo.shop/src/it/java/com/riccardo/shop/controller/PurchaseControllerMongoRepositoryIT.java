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
import com.riccardo.shop.model.Product;
import com.riccardo.shop.model.Purchase;
import com.riccardo.shop.repository.CustomerRepository;
import com.riccardo.shop.repository.ProductRepository;
import com.riccardo.shop.repository.PurchaseRepository;
import com.riccardo.shop.repository.RepositoryException;
import com.riccardo.shop.repository.mongo.CustomerMongoRepository;
import com.riccardo.shop.repository.mongo.ProductMongoRepository;
import com.riccardo.shop.repository.mongo.PurchaseMongoRepository;
import com.riccardo.shop.view.CustomerPurchaseView;

public class PurchaseControllerMongoRepositoryIT {

	private static final String CUSTOMER_ID = "C1";
	private static final String CUSTOMER_NAME = "customer1";

	private static final String PRODUCT_ID_1 = "P1";
	private static final String PRODUCT_NAME_1 = "product1";
	private static final double PRODUCT_PRICE_1 = 10.0;

	private static final String PRODUCT_ID_2 = "P2";
	private static final String PRODUCT_NAME_2 = "product2";
	private static final double PRODUCT_PRICE_2 = 20.0;

	public static final String SHOP_DB_NAME = "shop";
	public static final String PRODUCT_COLLECTION_NAME = "product";
	public static final String CUSTOMER_COLLECTION_NAME = "customer";
	public static final String PURCHASE_COLLECTION_NAME = "purchase";

	@ClassRule
	public static final MongoDBContainer mongo =
			new MongoDBContainer("mongo:5");

	@Mock
	private CustomerPurchaseView customerPurchaseView;

	private MongoClient client;

	private CustomerRepository customerRepository;
	private ProductRepository productRepository;
	private PurchaseRepository purchaseRepository;

	private PurchaseController purchaseController;

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
		productRepository = new ProductMongoRepository(client, SHOP_DB_NAME, PRODUCT_COLLECTION_NAME);
		purchaseRepository = new PurchaseMongoRepository(client, SHOP_DB_NAME, PURCHASE_COLLECTION_NAME);
		purchaseController = new PurchaseController(customerPurchaseView, purchaseRepository, customerRepository, productRepository);
	}

	@After
	public void tearDown() throws Exception {
		client.close();
		closeable.close();
	}

	@Test
	public void testAllCustomerPurchases() throws RepositoryException {
		Customer customer = new Customer(CUSTOMER_ID, CUSTOMER_NAME);
		Purchase purchase = new Purchase(CUSTOMER_ID, PRODUCT_ID_1);
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
		Purchase purchase = new Purchase(CUSTOMER_ID, PRODUCT_ID_1);
		purchaseRepository.save(purchase);
		purchaseController.deletePurchase(purchase);
		verify(customerPurchaseView).purchaseRemoved(purchase);
	}
}