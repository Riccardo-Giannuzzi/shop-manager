package com.riccardo.shop.bdd.steps;

import java.util.List;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;

public class DatabaseSteps {

	private static final int MONGO_PORT = 27017;
	private static final String MONGO_HOST = "localhost";

	public static final String SHOP_DB_NAME = "shop";
	public static final String PRODUCT_COLLECTION_NAME = "product";
	public static final String CUSTOMER_COLLECTION_NAME = "customer";
	public static final String PURCHASE_COLLECTION_NAME = "purchase";

	private static final String CUSTOMER_ID_KEY = "id";
	private static final String CUSTOMER_NAME_KEY = "name";
	private static final String PURCHASE_CUSTOMER_ID_KEY = "customerId";
	private static final String PURCHASE_PRODUCT_ID_KEY = "productId";

	private static final String PRODUCT_ID_KEY = "id";
	private static final String PRODUCT_NAME_KEY = "name";
	private static final String PRODUCT_PRICE_KEY = "price";

	static final String CUSTOMER_FIXTURE_1_ID = "C1";
	static final String CUSTOMER_FIXTURE_1_NAME = "customer1";
	static final String CUSTOMER_FIXTURE_2_ID = "C2";
	static final String CUSTOMER_FIXTURE_2_NAME = "customer2";

	static final String PRODUCT_FIXTURE_1_ID = "P1";
	static final String PRODUCT_FIXTURE_1_NAME = "product1";
	static final double PRODUCT_FIXTURE_1_PRICE = 10.0;

	static final String PRODUCT_FIXTURE_2_ID = "P2";
	static final String PRODUCT_FIXTURE_2_NAME = "product2";
	static final double PRODUCT_FIXTURE_2_PRICE = 20.0;

	private MongoClient mongoClient;

	@Before
	public void setUp() {
		mongoClient = new MongoClient(new ServerAddress(MONGO_HOST,MONGO_PORT));
		mongoClient.getDatabase(SHOP_DB_NAME).drop();
	}

	@After
	public void tearDown() {
		mongoClient.close();
	}

	@Given("The database contains the customers with the following values")
	public void the_database_contains_the_customers_with_the_following_values(List<List<String>> values) {
		values.forEach(
				v -> addTestCustomerToDatabase(v.get(0), v.get(1))
		);
	}

	@Given("The database contains the products with the following values")
	public void the_database_contains_the_products_with_the_following_values(List<List<String>> values) {
		values.forEach(
				v -> addTestProductToDatabase(v.get(0), v.get(1),Double.parseDouble(v.get(2)))
		);
	}

	@Given("The database contains the purchases with the following values")
	public void the_database_contains_the_purchases_with_the_following_values(List<List<String>> values) {
		values.forEach(
				v -> addTestPurchaseToDatabase(v.get(0), v.get(1))
		);
	}

	@Given("The database contains a few customers")
	public void the_database_contains_a_few_customers() {
		addTestCustomerToDatabase(CUSTOMER_FIXTURE_1_ID, CUSTOMER_FIXTURE_1_NAME);
		addTestCustomerToDatabase(CUSTOMER_FIXTURE_2_ID, CUSTOMER_FIXTURE_2_NAME);
	}

	@Given("The database contains a few products")
	public void the_database_contains_a_few_products() {
		addTestProductToDatabase(PRODUCT_FIXTURE_1_ID, PRODUCT_FIXTURE_1_NAME, PRODUCT_FIXTURE_1_PRICE);
		addTestProductToDatabase(PRODUCT_FIXTURE_2_ID, PRODUCT_FIXTURE_2_NAME, PRODUCT_FIXTURE_2_PRICE);
	}

	private void addTestCustomerToDatabase(String id, String name) {
		mongoClient
			.getDatabase(SHOP_DB_NAME)
			.getCollection(CUSTOMER_COLLECTION_NAME)
			.insertOne(
					new Document()
						.append(CUSTOMER_ID_KEY, id)
						.append(CUSTOMER_NAME_KEY, name));
	}

	private void addTestProductToDatabase(String id, String name, double price) {
		mongoClient
			.getDatabase(SHOP_DB_NAME)
			.getCollection(PRODUCT_COLLECTION_NAME)
			.insertOne(
					new Document()
						.append(PRODUCT_ID_KEY, id)
						.append(PRODUCT_NAME_KEY, name)
						.append(PRODUCT_PRICE_KEY, price));
	}

	private void addTestPurchaseToDatabase(String customerId, String productId) {
		mongoClient
			.getDatabase(SHOP_DB_NAME)
			.getCollection(PURCHASE_COLLECTION_NAME)
			.insertOne(
					new Document()
						.append(PURCHASE_CUSTOMER_ID_KEY, customerId)
						.append(PURCHASE_PRODUCT_ID_KEY, productId));
	}

}
