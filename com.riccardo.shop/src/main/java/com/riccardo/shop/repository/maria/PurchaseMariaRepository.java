package com.riccardo.shop.repository.maria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.riccardo.shop.model.Purchase;
import com.riccardo.shop.repository.PurchaseRepository;
import com.riccardo.shop.repository.RepositoryException;

public class PurchaseMariaRepository implements PurchaseRepository {

	public static final String PURCHASE_TABLE_NAME = "purchase";
	public static final String CUSTOMER_ID_KEY = "customer_id";
	public static final String PRODUCT_ID_KEY = "product_id";

	private final Connection connection;

	public PurchaseMariaRepository(Connection connection) {
		this.connection = connection;
	}

	@Override
	public List<Purchase> findAll() throws RepositoryException {
		List<Purchase> purchases = new ArrayList<>();
		String query =
				"SELECT " + CUSTOMER_ID_KEY + ", " + PRODUCT_ID_KEY
				+ " FROM " + PURCHASE_TABLE_NAME;
		try (PreparedStatement statement = connection.prepareStatement(query);
				ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				purchases.add(fromResultSetToPurchase(resultSet));
			}
			return purchases;
		} catch (SQLException e) {
			throw new RepositoryException("Error finding purchases", e);
		}
	}

	@Override
	public List<Purchase> findByCustomerId(String customerId) throws RepositoryException {
		List<Purchase> purchases = new ArrayList<>();
		String query =
				"SELECT " + CUSTOMER_ID_KEY + ", " + PRODUCT_ID_KEY
				+ " FROM " + PURCHASE_TABLE_NAME
				+ " WHERE " + CUSTOMER_ID_KEY + " = ?";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, customerId);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					purchases.add(fromResultSetToPurchase(resultSet));
				}
			}
			return purchases;
		} catch (SQLException e) {
			throw new RepositoryException("Error finding purchases by customer id", e);
		}
	}

	@Override
	public List<Purchase> findByProductId(String productId) throws RepositoryException {
		List<Purchase> purchases = new ArrayList<>();
		String query =
				"SELECT " + CUSTOMER_ID_KEY + ", " + PRODUCT_ID_KEY
				+ " FROM " + PURCHASE_TABLE_NAME
				+ " WHERE " + PRODUCT_ID_KEY + " = ?";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, productId);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					purchases.add(fromResultSetToPurchase(resultSet));
				}
			}
			return purchases;
		} catch (SQLException e) {
			throw new RepositoryException("Error finding purchases by product id", e);
		}
	}

	@Override
	public Purchase findByCustomerIdAndProductId(String customerId, String productId) throws RepositoryException {
		String query =
				"SELECT " + CUSTOMER_ID_KEY + ", " + PRODUCT_ID_KEY
				+ " FROM " + PURCHASE_TABLE_NAME
				+ " WHERE " + CUSTOMER_ID_KEY + " = ?"
				+ " AND " + PRODUCT_ID_KEY + " = ?";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, customerId);
			statement.setString(2, productId);
			try (ResultSet resultSet = statement.executeQuery()) {
				Purchase purchase = null;
				if (resultSet.next()) {
					purchase = fromResultSetToPurchase(resultSet);
				}
				return purchase;
			}
		} catch (SQLException e) {
			throw new RepositoryException("Error finding purchase", e);
		}
	}

	@Override
	public void save(Purchase purchase) throws RepositoryException {
		String query =
				"INSERT INTO " + PURCHASE_TABLE_NAME
				+ " (" + CUSTOMER_ID_KEY + ", " + PRODUCT_ID_KEY + ") "
				+ "VALUES (?, ?)";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, purchase.getCustomerId());
			statement.setString(2, purchase.getProductId());
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new RepositoryException("Error saving purchase", e);
		}
	}

	@Override
	public void delete(String customerId, String productId) throws RepositoryException {
		String query =
				"DELETE FROM " + PURCHASE_TABLE_NAME
				+ " WHERE " + CUSTOMER_ID_KEY + " = ?"
				+ " AND " + PRODUCT_ID_KEY + " = ?";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, customerId);
			statement.setString(2, productId);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new RepositoryException("Error deleting purchase", e);
		}
	}

	private Purchase fromResultSetToPurchase(ResultSet resultSet)
			throws SQLException {
		return new Purchase(
				resultSet.getString(CUSTOMER_ID_KEY),
				resultSet.getString(PRODUCT_ID_KEY));
	}
}
