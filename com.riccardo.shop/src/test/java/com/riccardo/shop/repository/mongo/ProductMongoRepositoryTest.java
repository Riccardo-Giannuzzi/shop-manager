package com.riccardo.shop.repository.mongo;

import static org.assertj.core.api.Assertions.*;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.riccardo.shop.model.Product;
import com.riccardo.shop.repository.RepositoryException;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

public class ProductMongoRepositoryTest {

	private static final String PRODUCT_ID_1 = "P1";
	private static final String PRODUCT_NAME_1 = "product_1";
	private static final double PRODUCT_PRICE_1 = 10.0;

	private static final String PRODUCT_ID_2 = "P2";
	private static final String PRODUCT_NAME_2 = "product_2";
	private static final double PRODUCT_PRICE_2 = 20.0;

	private static MongoServer server;
	private static InetSocketAddress serverAddress;

	private MongoClient client;
	private ProductMongoRepository productRepository;
	private MongoCollection<Document> productCollection;

	@BeforeClass
	public static void setupServer() {
		server = new MongoServer(new MemoryBackend());
		serverAddress = server.bind();
	}

	@AfterClass
	public static void shutdownServer() {
		server.shutdown();
	}

	@Before
	public void setup() {
		client = new MongoClient(new ServerAddress(serverAddress));
		MongoDatabase database = client.getDatabase(ProductMongoRepository.SHOP_DB_NAME);
		database.drop();
		productRepository = new ProductMongoRepository(client);
		productCollection = database.getCollection(ProductMongoRepository.PRODUCT_COLLECTION_NAME);
	}

	@After
	public void tearDown() {
		client.close();
	}

	@Test
	public void testFindAllWhenDatabaseIsEmpty() throws RepositoryException {
		assertThat(productRepository.findAll())
			.isEmpty();
	}

	@Test
	public void testFindAllWhenDatabaseIsNotEmpty() throws RepositoryException {
		addTestProductToDatabase(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		addTestProductToDatabase(PRODUCT_ID_2, PRODUCT_NAME_2, PRODUCT_PRICE_2);
		assertThat(productRepository.findAll())
			.containsExactlyInAnyOrder(
					new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1),
					new Product(PRODUCT_ID_2, PRODUCT_NAME_2, PRODUCT_PRICE_2));
	}
	
	@Test
	public void testFindByIdWhenProductDoesNotExist() throws RepositoryException {
		addTestProductToDatabase(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		assertThat(productRepository.findById(PRODUCT_ID_2))
			.isNull();
	}

	@Test
	public void testFindByIdWhenProductExists() throws RepositoryException {
		addTestProductToDatabase(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		addTestProductToDatabase(PRODUCT_ID_2, PRODUCT_NAME_2, PRODUCT_PRICE_2);
		assertThat(productRepository.findById(PRODUCT_ID_2))
			.isEqualTo(new Product(PRODUCT_ID_2, PRODUCT_NAME_2, PRODUCT_PRICE_2));
	}

	@Test
	public void testSaveWhenProductDoesNotAlreadyExist() throws RepositoryException {
		Product product = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		productRepository.save(product);
		assertThat(readAllProductsFromDatabase())
			.containsExactly(product);
	}

	@Test
	public void testSaveWhenProductAlreadyExistsThrowsRepositoryException() throws RepositoryException {
		Product product = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		Product productWithSameId = new Product(PRODUCT_ID_1, PRODUCT_NAME_2, PRODUCT_PRICE_2);
		productRepository.save(product);
		assertThatThrownBy(
				() -> productRepository.save(productWithSameId))
			.isInstanceOf(RepositoryException.class);
	}

	@Test
	public void testDeleteWhenProductExists() throws RepositoryException {
		addTestProductToDatabase(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		productRepository.delete(PRODUCT_ID_1);
		assertThat(readAllProductsFromDatabase())
			.isEmpty();
	}

	@Test
	public void testDeleteWhenProductDoesNotExist() throws RepositoryException {
		addTestProductToDatabase(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		productRepository.delete(PRODUCT_ID_2);
		assertThat(readAllProductsFromDatabase())
			.containsExactly(
					new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1));
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
				.collect(Collectors.toList());
	}
}
