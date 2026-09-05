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
import com.riccardo.shop.model.Product;
import com.riccardo.shop.repository.RepositoryException;

public class ProductMongoRepositoryIT {

	private static final String PRODUCT_ID_1 = "P1";
	private static final String PRODUCT_NAME_1 = "product_1";
	private static final double PRODUCT_PRICE_1 = 10.0;

	private static final String PRODUCT_ID_2 = "P2";
	private static final String PRODUCT_NAME_2 = "product_2";
	private static final double PRODUCT_PRICE_2 = 20.0;

	public static final String SHOP_DB_NAME = "shop";
	public static final String PRODUCT_COLLECTION_NAME = "product";

	@ClassRule
	public static final MongoDBContainer mongo = new MongoDBContainer("mongo:5");

	private MongoClient client;
	private ProductMongoRepository productRepository;
	private MongoCollection<Document> productCollection;

	@Before
	public void setup() {
		client = new MongoClient(
				new ServerAddress(
						mongo.getHost(),
						mongo.getFirstMappedPort()));
		MongoDatabase database = client.getDatabase(SHOP_DB_NAME);
		database.drop();
		productRepository = new ProductMongoRepository(client, SHOP_DB_NAME, PRODUCT_COLLECTION_NAME);
		productCollection = database.getCollection(PRODUCT_COLLECTION_NAME);
	}

	@After
	public void tearDown() {
		client.close();
	}

	@Test
	public void testFindAll() throws RepositoryException {
		addTestProductToDatabase(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		addTestProductToDatabase(PRODUCT_ID_2, PRODUCT_NAME_2, PRODUCT_PRICE_2);
		assertThat(productRepository.findAll())
			.containsExactly(
					new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1),
					new Product(PRODUCT_ID_2, PRODUCT_NAME_2, PRODUCT_PRICE_2));
	}

	@Test
	public void testFindById() throws RepositoryException {
		addTestProductToDatabase(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		addTestProductToDatabase(PRODUCT_ID_2, PRODUCT_NAME_2, PRODUCT_PRICE_2);
		assertThat(productRepository.findById(PRODUCT_ID_2))
			.isEqualTo(new Product(PRODUCT_ID_2, PRODUCT_NAME_2, PRODUCT_PRICE_2));
	}

	@Test
	public void testSave() throws RepositoryException {
		Product product =
				new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		productRepository.save(product);
		assertThat(readAllProductsFromDatabase())
			.containsExactly(product);
	}

	@Test
	public void testSaveDuplicateProductThrowsRepositoryException() throws RepositoryException {
		Product product = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		productRepository.save(product);
		assertThatThrownBy(() -> productRepository.save(product))
			.isInstanceOf(RepositoryException.class);
	}

	@Test
	public void testDelete() throws RepositoryException {
		addTestProductToDatabase(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		productRepository.delete(PRODUCT_ID_1);
		assertThat(readAllProductsFromDatabase())
			.isEmpty();
	}

	private void addTestProductToDatabase(String id, String name, double price) {
		productCollection.insertOne(
				new Document()
					.append(ProductMongoRepository.PRODUCT_ID_KEY, id)
					.append(ProductMongoRepository.PRODUCT_NAME_KEY, name)
					.append(ProductMongoRepository.PRODUCT_PRICE_KEY, price));
	}

	private List<Product> readAllProductsFromDatabase() {
		return StreamSupport
				.stream(productCollection.find().spliterator(), false)
				.map(d -> new Product(
						"" + d.get(ProductMongoRepository.PRODUCT_ID_KEY),
						"" + d.get(ProductMongoRepository.PRODUCT_NAME_KEY),
						d.getDouble(ProductMongoRepository.PRODUCT_PRICE_KEY)))
				.toList();
	}
}
