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
import com.riccardo.shop.model.Customer;
import com.riccardo.shop.repository.RepositoryException;

public class CustomerMongoRepositoryIT {

	private static final String CUSTOMER_ID_1 = "C1";
	private static final String CUSTOMER_NAME_1 = "customer_1";

	private static final String CUSTOMER_ID_2 = "C2";
	private static final String CUSTOMER_NAME_2 = "customer_2";

	@ClassRule
	public static final MongoDBContainer mongo = new MongoDBContainer("mongo:5");

	private MongoClient client;
	private CustomerMongoRepository customerRepository;
	private MongoCollection<Document> customerCollection;

	@Before
	public void setup() {
		client = new MongoClient(
				new ServerAddress(
						mongo.getHost(),
						mongo.getFirstMappedPort()));
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
	public void testFindAll() throws RepositoryException {
		addTestCustomerToDatabase(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		addTestCustomerToDatabase(CUSTOMER_ID_2, CUSTOMER_NAME_2);
		assertThat(customerRepository.findAll())
			.containsExactly(
					new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1),
					new Customer(CUSTOMER_ID_2, CUSTOMER_NAME_2));
	}

	@Test
	public void testFindById() throws RepositoryException {
		addTestCustomerToDatabase(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		addTestCustomerToDatabase(CUSTOMER_ID_2, CUSTOMER_NAME_2);
		assertThat(customerRepository.findById(CUSTOMER_ID_2))
			.isEqualTo(new Customer(CUSTOMER_ID_2, CUSTOMER_NAME_2));
	}

	@Test
	public void testSave() throws RepositoryException {
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		customerRepository.save(customer);
		assertThat(readAllCustomersFromDatabase())
			.containsExactly(customer);
	}

	@Test
	public void testSaveDuplicateCustomerThrowsRepositoryException() throws RepositoryException {
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		customerRepository.save(customer);
		assertThatThrownBy(() -> customerRepository.save(customer))
			.isInstanceOf(RepositoryException.class);
	}

	@Test
	public void testDelete() throws RepositoryException {
		addTestCustomerToDatabase(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		customerRepository.delete(CUSTOMER_ID_1);
		assertThat(readAllCustomersFromDatabase())
			.isEmpty();
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
