package com.riccardo.shop.repository.mongo;

import static org.assertj.core.api.Assertions.*;

import java.net.InetSocketAddress;
import java.util.List;
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
import com.riccardo.shop.model.Customer;
import com.riccardo.shop.repository.RepositoryException;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

public class CustomerMongoRepositoryTest {

	private static final String CUSTOMER_ID_1 = "C1";
	private static final String CUSTOMER_NAME_1 = "customer_1";

	private static final String CUSTOMER_ID_2 = "C2";
	private static final String CUSTOMER_NAME_2 = "customer_2";

	private static MongoServer server;
	private static InetSocketAddress serverAddress;

	private MongoClient client;
	private CustomerMongoRepository customerRepository;
	private MongoCollection<Document> customerCollection;

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
		MongoDatabase database = client.getDatabase(CustomerMongoRepository.SHOP_DB_NAME);
		database.drop();
		customerRepository = new CustomerMongoRepository(client);
		customerCollection = database.getCollection(CustomerMongoRepository.CUSTOMER_COLLECTION_NAME);
	}

	@After
	public void tearDown() {
		client.close();
	}

	@Test
	public void testFindAllWhenDatabaseIsEmpty() throws RepositoryException {
		assertThat(customerRepository.findAll())
			.isEmpty();
	}

	@Test
	public void testFindAllWhenDatabaseIsNotEmpty() throws RepositoryException {
		addTestCustomerToDatabase(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		addTestCustomerToDatabase(CUSTOMER_ID_2, CUSTOMER_NAME_2);
		assertThat(customerRepository.findAll())
			.containsExactlyInAnyOrder(
					new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1),
					new Customer(CUSTOMER_ID_2, CUSTOMER_NAME_2));
	}

	@Test
	public void testFindByIdWhenCustomerDoesNotExist() throws RepositoryException {
		addTestCustomerToDatabase(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		assertThat(customerRepository.findById(CUSTOMER_ID_2))
			.isNull();
	}

	@Test
	public void testFindByIdWhenCustomerExists() throws RepositoryException {
		addTestCustomerToDatabase(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		addTestCustomerToDatabase(CUSTOMER_ID_2, CUSTOMER_NAME_2);
		assertThat(customerRepository.findById(CUSTOMER_ID_2))
			.isEqualTo(new Customer(CUSTOMER_ID_2, CUSTOMER_NAME_2));
	}

	@Test
	public void testSaveWhenCustomerDoesNotAlreadyExist() throws RepositoryException {
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		customerRepository.save(customer);
		assertThat(readAllCustomersFromDatabase())
			.containsExactly(customer);
	}

	@Test
	public void testSaveWhenCustomerAlreadyExistsThrowsRepositoryException() throws RepositoryException {
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		Customer customerWithSameId = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_2);
		customerRepository.save(customer);
		assertThatThrownBy(
				() -> customerRepository.save(customerWithSameId))
			.isInstanceOf(RepositoryException.class);
	}

	@Test
	public void testDeleteWhenCustomerExists() throws RepositoryException {
		addTestCustomerToDatabase(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		customerRepository.delete(CUSTOMER_ID_1);
		assertThat(readAllCustomersFromDatabase())
			.isEmpty();
	}

	@Test
	public void testDeleteWhenCustomerDoesNotExist() throws RepositoryException {
		addTestCustomerToDatabase(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		customerRepository.delete(CUSTOMER_ID_2);
		assertThat(readAllCustomersFromDatabase())
			.containsExactly(
					new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1));
	}

	private void addTestCustomerToDatabase(String id, String name) {
		customerCollection.insertOne(
				new Document()
					.append(CustomerMongoRepository.CUSTOMER_ID_KEY, id)
					.append(CustomerMongoRepository.CUSTOMER_NAME_KEY, name));
	}

	private List<Customer> readAllCustomersFromDatabase() {
		return StreamSupport
				.stream(customerCollection.find().spliterator(), false)
				.map(d -> new Customer(
						"" + d.get(CustomerMongoRepository.CUSTOMER_ID_KEY),
						"" + d.get(CustomerMongoRepository.CUSTOMER_NAME_KEY)))
				.toList();
	}
}
