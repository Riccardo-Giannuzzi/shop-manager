package com.riccardo.shop.repository.mongo;

import java.util.List;
import java.util.stream.StreamSupport;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.riccardo.shop.model.Customer;
import com.riccardo.shop.repository.CustomerRepository;
import com.riccardo.shop.repository.RepositoryException;

public class CustomerMongoRepository implements CustomerRepository {

	public static final String CUSTOMER_ID_KEY = "id";
	public static final String CUSTOMER_NAME_KEY = "name";
	public static final String SHOP_DB_NAME = "shop";
	public static final String CUSTOMER_COLLECTION_NAME = "customer";
	private MongoCollection<Document> customerCollection;

	public CustomerMongoRepository(MongoClient client) {
		customerCollection = client
			.getDatabase(SHOP_DB_NAME)
			.getCollection(CUSTOMER_COLLECTION_NAME);
		customerCollection.createIndex(
				Indexes.ascending(CUSTOMER_ID_KEY),
				new IndexOptions().unique(true));
	}

	@Override
	public List<Customer> findAll() throws RepositoryException {
		return StreamSupport
				.stream(customerCollection.find().spliterator(), false)
				.map(this::fromDocumentToCustomer)
				.toList();
	}

	@Override
	public Customer findById(String id) throws RepositoryException {
		Document document = customerCollection
				.find(Filters.eq(CUSTOMER_ID_KEY, id))
				.first();
		if (document != null) {
			return fromDocumentToCustomer(document);
		}
		return null;
	}

	@Override
	public void save(Customer customer) throws RepositoryException {
		try {
			customerCollection.insertOne(
					new Document()
						.append(CUSTOMER_ID_KEY, customer.getId())
						.append(CUSTOMER_NAME_KEY, customer.getName()));
		} catch (MongoException e) {
			throw new RepositoryException("Error saving customer", e);
		}
	}

	@Override
	public void delete(String id) throws RepositoryException {
		customerCollection.deleteOne(Filters.eq(CUSTOMER_ID_KEY, id));
	}

	private Customer fromDocumentToCustomer(Document document) {
		return new Customer(
				"" + document.get(CUSTOMER_ID_KEY),
				"" + document.get(CUSTOMER_NAME_KEY));
	}

}
