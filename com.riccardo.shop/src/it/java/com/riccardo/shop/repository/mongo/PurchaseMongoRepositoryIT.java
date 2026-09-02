package com.riccardo.shop.repository.mongo;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.MongoDBContainer;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.riccardo.shop.model.Purchase;
import com.riccardo.shop.repository.RepositoryException;

public class PurchaseMongoRepositoryIT {

	private static final String CUSTOMER_ID_1 = "C1";
	private static final String CUSTOMER_ID_2 = "C2";

	private static final String PRODUCT_ID_1 = "P1";
	private static final String PRODUCT_ID_2 = "P2";

	@ClassRule
	public static final MongoDBContainer mongo = new MongoDBContainer("mongo:5");

	private MongoClient client;
	private PurchaseMongoRepository purchaseRepository;
	private MongoCollection<Document> purchaseCollection;

	@Before
	public void setup() {
		client = new MongoClient(
				new ServerAddress(
						mongo.getHost(),
						mongo.getFirstMappedPort()));
		MongoDatabase database = client.getDatabase(PurchaseMongoRepository.SHOP_DB_NAME);
		database.drop();
		purchaseRepository = new PurchaseMongoRepository(client);
		purchaseCollection = database.getCollection(PurchaseMongoRepository.PURCHASE_COLLECTION_NAME);
	}

	@After
	public void tearDown() {
		client.close();
	}

	@Test
	public void testFindAll() throws RepositoryException {
		addTestPurchaseToDatabase(CUSTOMER_ID_1, PRODUCT_ID_1);
		addTestPurchaseToDatabase(CUSTOMER_ID_2, PRODUCT_ID_2);
		assertThat(purchaseRepository.findAll())
			.containsExactly(
					new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1),
					new Purchase(CUSTOMER_ID_2, PRODUCT_ID_2));
	}

	@Test
	public void testFindByCustomerId() throws RepositoryException {
		addTestPurchaseToDatabase(CUSTOMER_ID_1, PRODUCT_ID_1);
		addTestPurchaseToDatabase(CUSTOMER_ID_1, PRODUCT_ID_2);
		addTestPurchaseToDatabase(CUSTOMER_ID_2, PRODUCT_ID_1);
		assertThat(purchaseRepository.findByCustomerId(CUSTOMER_ID_1))
			.containsExactly(
					new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1),
					new Purchase(CUSTOMER_ID_1, PRODUCT_ID_2));
	}

	@Test
	public void testFindByProductId() throws RepositoryException {
		addTestPurchaseToDatabase(CUSTOMER_ID_1, PRODUCT_ID_1);
		addTestPurchaseToDatabase(CUSTOMER_ID_2, PRODUCT_ID_1);
		addTestPurchaseToDatabase(CUSTOMER_ID_1, PRODUCT_ID_2);
		assertThat(purchaseRepository.findByProductId(PRODUCT_ID_1))
			.containsExactly(
					new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1),
					new Purchase(CUSTOMER_ID_2, PRODUCT_ID_1));
	}

	@Test
	public void testFindByCustomerIdAndProductId() throws RepositoryException {
		addTestPurchaseToDatabase(CUSTOMER_ID_1, PRODUCT_ID_1);
		addTestPurchaseToDatabase(CUSTOMER_ID_1, PRODUCT_ID_2);
		assertThat(
				purchaseRepository.findByCustomerIdAndProductId(
						CUSTOMER_ID_1,
						PRODUCT_ID_2))
			.isEqualTo(new Purchase(CUSTOMER_ID_1, PRODUCT_ID_2));
	}

	@Test
	public void testSave() throws RepositoryException {
		Purchase purchase = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		purchaseRepository.save(purchase);
		assertThat(readAllPurchasesFromDatabase())
			.containsExactly(purchase);
	}

	@Test
	public void testSaveDuplicatePurchaseThrowsRepositoryException()
			throws RepositoryException {
		Purchase purchase = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		purchaseRepository.save(purchase);
		assertThatThrownBy(() -> purchaseRepository.save(purchase))
			.isInstanceOf(RepositoryException.class);
	}

	@Test
	public void testDelete() throws RepositoryException {
		addTestPurchaseToDatabase(CUSTOMER_ID_1, PRODUCT_ID_1);
		addTestPurchaseToDatabase(CUSTOMER_ID_1, PRODUCT_ID_2);
		purchaseRepository.delete(CUSTOMER_ID_1, PRODUCT_ID_1);
		assertThat(readAllPurchasesFromDatabase())
			.containsExactly(
					new Purchase(CUSTOMER_ID_1, PRODUCT_ID_2));
	}

	private void addTestPurchaseToDatabase(String customerId, String productId) {
		purchaseCollection.insertOne(
				new Document()
					.append(PurchaseMongoRepository.CUSTOMER_ID_KEY, customerId)
					.append(PurchaseMongoRepository.PRODUCT_ID_KEY, productId));
	}

	private List<Purchase> readAllPurchasesFromDatabase() {
		return StreamSupport
				.stream(purchaseCollection.find().spliterator(), false)
				.map(d -> new Purchase(
						"" + d.get(PurchaseMongoRepository.CUSTOMER_ID_KEY),
						"" + d.get(PurchaseMongoRepository.PRODUCT_ID_KEY)))
				.toList();
	}
}
