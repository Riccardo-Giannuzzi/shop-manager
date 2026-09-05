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
import com.riccardo.shop.model.Purchase;
import com.riccardo.shop.repository.PurchaseRepository;
import com.riccardo.shop.repository.RepositoryException;

public class PurchaseMongoRepository implements PurchaseRepository {

	public static final String CUSTOMER_ID_KEY = "customerId";
	public static final String PRODUCT_ID_KEY = "productId";

	private MongoCollection<Document> purchaseCollection;

	public PurchaseMongoRepository(MongoClient client, String databaseName, String collectionName) {
		purchaseCollection = client
			.getDatabase(databaseName)
			.getCollection(collectionName);
		purchaseCollection.createIndex(
				Indexes.ascending(CUSTOMER_ID_KEY, PRODUCT_ID_KEY),
				new IndexOptions().unique(true));
	}

	@Override
	public List<Purchase> findAll() throws RepositoryException {
		return StreamSupport
				.stream(purchaseCollection.find().spliterator(), false)
				.map(this::fromDocumentToPurchase)
				.toList();
	}

	@Override
	public List<Purchase> findByCustomerId(String customerId) throws RepositoryException {
		return StreamSupport
				.stream(purchaseCollection.find(Filters.eq(CUSTOMER_ID_KEY, customerId)).spliterator(), false)
				.map(this::fromDocumentToPurchase)
				.toList();
	}

	@Override
	public List<Purchase> findByProductId(String productId) throws RepositoryException {
		return StreamSupport
				.stream(purchaseCollection.find(Filters.eq(PRODUCT_ID_KEY, productId)).spliterator(), false)
				.map(this::fromDocumentToPurchase)
				.toList();
	}

	@Override
	public Purchase findByCustomerIdAndProductId(String customerId, String productId) throws RepositoryException {
		Document document = purchaseCollection
				.find(Filters.and(
						Filters.eq(CUSTOMER_ID_KEY, customerId),
						Filters.eq(PRODUCT_ID_KEY, productId)))
				.first();
		if (document != null) {
			return fromDocumentToPurchase(document);
		}
		return null;
	}

	@Override
	public void save(Purchase purchase) throws RepositoryException {
		try {
			purchaseCollection.insertOne(
					new Document()
						.append(CUSTOMER_ID_KEY, purchase.getCustomerId())
						.append(PRODUCT_ID_KEY, purchase.getProductId()));
		} catch (MongoException e) {
			throw new RepositoryException("Error saving purchase", e);
		}
	}

	@Override
	public void delete(String customerId, String productId) throws RepositoryException {
		purchaseCollection.deleteOne(
				Filters.and(
						Filters.eq(CUSTOMER_ID_KEY, customerId),
						Filters.eq(PRODUCT_ID_KEY, productId)));
	}

	private Purchase fromDocumentToPurchase(Document document) {
		return new Purchase(
				"" + document.get(CUSTOMER_ID_KEY),
				"" + document.get(PRODUCT_ID_KEY));
	}

}
