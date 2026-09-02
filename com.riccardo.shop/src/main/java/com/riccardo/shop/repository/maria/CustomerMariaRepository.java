package com.riccardo.shop.repository.maria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.riccardo.shop.model.Customer;
import com.riccardo.shop.repository.CustomerRepository;
import com.riccardo.shop.repository.RepositoryException;

public class CustomerMariaRepository implements CustomerRepository {

	public static final String CUSTOMER_TABLE_NAME = "customer";
	public static final String CUSTOMER_ID_KEY = "id";
	public static final String CUSTOMER_NAME_KEY = "name";

	private final Connection connection;

	public CustomerMariaRepository(Connection connection) {
		this.connection = connection;
	}

	@Override
	public List<Customer> findAll() throws RepositoryException {
		List<Customer> customers = new ArrayList<>();
		String query =
				"SELECT " + CUSTOMER_ID_KEY + ", " + CUSTOMER_NAME_KEY
				+ " FROM " + CUSTOMER_TABLE_NAME;
		try (PreparedStatement statement = connection.prepareStatement(query);
				ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				customers.add(fromResultSetToCustomer(resultSet));
			}
			return customers;
		} catch (SQLException e) {
			throw new RepositoryException("Error finding customers", e);
		}
	}

	@Override
	public Customer findById(String id) throws RepositoryException {
		String query =
				"SELECT " + CUSTOMER_ID_KEY + ", " + CUSTOMER_NAME_KEY
				+ " FROM " + CUSTOMER_TABLE_NAME
				+ " WHERE " + CUSTOMER_ID_KEY + " = ?";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, id);
			try (ResultSet resultSet = statement.executeQuery()) {
				Customer customer = null;
				if (resultSet.next()) {
					customer = fromResultSetToCustomer(resultSet);
				}
				return customer;
			}
		} catch (SQLException e) {
			throw new RepositoryException("Error finding customer", e);
		}
	}

	@Override
	public void save(Customer customer) throws RepositoryException {
		String query =
				"INSERT INTO " + CUSTOMER_TABLE_NAME
				+ " (" + CUSTOMER_ID_KEY + ", " + CUSTOMER_NAME_KEY + ") "
				+ "VALUES (?, ?)";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, customer.getId());
			statement.setString(2, customer.getName());
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new RepositoryException("Error saving customer", e);
		}
	}

	@Override
	public void delete(String id) throws RepositoryException {
		String query =
				"DELETE FROM " + CUSTOMER_TABLE_NAME
				+ " WHERE " + CUSTOMER_ID_KEY + " = ?";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, id);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new RepositoryException("Error deleting customer", e);
		}
	}

	private Customer fromResultSetToCustomer(ResultSet resultSet)
			throws SQLException {
		return new Customer(
				resultSet.getString(CUSTOMER_ID_KEY),
				resultSet.getString(CUSTOMER_NAME_KEY));
	}
}