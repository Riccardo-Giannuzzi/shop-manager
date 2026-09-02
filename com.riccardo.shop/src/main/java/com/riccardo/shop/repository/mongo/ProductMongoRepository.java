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
import com.riccardo.shop.model.Product;
import com.riccardo.shop.repository.ProductRepository;
import com.riccardo.shop.repository.RepositoryException;

public class ProductMongoRepository implements ProductRepository {

	public static final String PRODUCT_ID_KEY = "id";
	public static final String PRODUCT_NAME_KEY = "name";
	public static final String PRODUCT_PRICE_KEY = "price";
	public static final String SHOP_DB_NAME = "shop";
	public static final String PRODUCT_COLLECTION_NAME = "product";
	private MongoCollection<Document> productCollection;

	public ProductMongoRepository(MongoClient client) {
		productCollection = client
			.getDatabase(SHOP_DB_NAME)
			.getCollection(PRODUCT_COLLECTION_NAME);
		productCollection.createIndex(
				Indexes.ascending(PRODUCT_ID_KEY),
				new IndexOptions().unique(true));
	}

	@Override
	public List<Product> findAll() throws RepositoryException {
		return StreamSupport
				.stream(productCollection.find().spliterator(), false)
				.map(this::fromDocumentToProduct)
				.toList();
	}

	@Override
	public Product findById(String id) throws RepositoryException {
		Document document = productCollection
				.find(Filters.eq(PRODUCT_ID_KEY, id))
				.first();
		if (document != null) {
			return fromDocumentToProduct(document);
		}
		return null;
	}

	@Override
	public void save(Product product) throws RepositoryException {
		try {
			productCollection.insertOne(
					new Document()
						.append(PRODUCT_ID_KEY, product.getId())
						.append(PRODUCT_NAME_KEY, product.getName())
						.append(PRODUCT_PRICE_KEY, product.getPrice()));
		} catch (MongoException e) {
			throw new RepositoryException("Error saving product", e);
		}
	}

	@Override
	public void delete(String id) throws RepositoryException {
		productCollection.deleteOne(Filters.eq(PRODUCT_ID_KEY, id));
	}

	private Product fromDocumentToProduct(Document document) {
		return new Product(
				"" + document.get(PRODUCT_ID_KEY),
				"" + document.get(PRODUCT_NAME_KEY),
				document.getDouble(PRODUCT_PRICE_KEY));
	}

}
