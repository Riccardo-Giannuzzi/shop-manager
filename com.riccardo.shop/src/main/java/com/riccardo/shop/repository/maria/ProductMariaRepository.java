package com.riccardo.shop.repository.maria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.riccardo.shop.model.Product;
import com.riccardo.shop.repository.ProductRepository;
import com.riccardo.shop.repository.RepositoryException;

public class ProductMariaRepository implements ProductRepository {

	public static final String PRODUCT_TABLE_NAME = "product";
	public static final String PRODUCT_ID_KEY = "id";
	public static final String PRODUCT_NAME_KEY = "name";
	public static final String PRODUCT_PRICE_KEY = "price";

	private final Connection connection;

	public ProductMariaRepository(Connection connection) {
		this.connection = connection;
	}

	@Override
	public List<Product> findAll() throws RepositoryException {
		List<Product> products = new ArrayList<>();
		String query =
				"SELECT " + PRODUCT_ID_KEY + ", " + PRODUCT_NAME_KEY + ", " + PRODUCT_PRICE_KEY
				+ " FROM " + PRODUCT_TABLE_NAME;
		try (PreparedStatement statement = connection.prepareStatement(query);
				ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				products.add(fromResultSetToProduct(resultSet));
			}
			return products;
		} catch (SQLException e) {
			throw new RepositoryException("Error finding products", e);
		}
	}

	@Override
	public Product findById(String id) throws RepositoryException {
		String query =
				"SELECT " + PRODUCT_ID_KEY + ", " + PRODUCT_NAME_KEY + ", " + PRODUCT_PRICE_KEY
				+ " FROM " + PRODUCT_TABLE_NAME
				+ " WHERE " + PRODUCT_ID_KEY + " = ?";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, id);
			try (ResultSet resultSet = statement.executeQuery()) {
				Product product = null;
				if (resultSet.next()) {
					product = fromResultSetToProduct(resultSet);
				}
				return product;
			}
		} catch (SQLException e) {
			throw new RepositoryException("Error finding product", e);
		}
	}

	@Override
	public void save(Product product) throws RepositoryException {
		String query =
				"INSERT INTO " + PRODUCT_TABLE_NAME 
				+ " (" + PRODUCT_ID_KEY + ", " + PRODUCT_NAME_KEY + ", " + PRODUCT_PRICE_KEY + ") "
				+ "VALUES (?, ?, ?)";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, product.getId());
			statement.setString(2, product.getName());
			statement.setDouble(3, product.getPrice());
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new RepositoryException("Error saving product", e);
		}
	}

	@Override
	public void delete(String id) throws RepositoryException {
		String query =
				"DELETE FROM " + PRODUCT_TABLE_NAME
				+ " WHERE " + PRODUCT_ID_KEY + " = ?";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, id);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new RepositoryException("Error deleting product", e);
		}
	}

	private Product fromResultSetToProduct(ResultSet resultSet) throws SQLException {
		return new Product(
				resultSet.getString(PRODUCT_ID_KEY),
				resultSet.getString(PRODUCT_NAME_KEY),
				resultSet.getDouble(PRODUCT_PRICE_KEY));
	}
}