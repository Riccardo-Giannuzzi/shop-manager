package com.riccardo.shop.repository.maria;

import static org.assertj.core.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.MariaDBContainer;

import com.riccardo.shop.model.Customer;
import com.riccardo.shop.repository.RepositoryException;

public class CustomerMariaRepositoryTest {

	private static final String MARIADB_IMAGE = "mariadb:11";

	private static final String CUSTOMER_ID_1 = "C1";
	private static final String CUSTOMER_NAME_1 = "customer_1";

	private static final String CUSTOMER_ID_2 = "C2";
	private static final String CUSTOMER_NAME_2 = "customer_2";

	@ClassRule
	public static final MariaDBContainer<?> mariadb = new MariaDBContainer<>(MARIADB_IMAGE);

	private static Connection connection;
	private CustomerMariaRepository customerRepository;

	@BeforeClass
	public static void setupConnection() throws SQLException {
		connection = DriverManager.getConnection(
				mariadb.getJdbcUrl(),
				mariadb.getUsername(),
				mariadb.getPassword());
	}

	@Before
	public void setup() throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute("DROP TABLE IF EXISTS " + CustomerMariaRepository.CUSTOMER_TABLE_NAME);
			statement.execute(
					"CREATE TABLE " + CustomerMariaRepository.CUSTOMER_TABLE_NAME + " ("
					+ CustomerMariaRepository.CUSTOMER_ID_KEY + " VARCHAR(255) PRIMARY KEY, "
					+ CustomerMariaRepository.CUSTOMER_NAME_KEY + " VARCHAR(255) NOT NULL)");
		}
		customerRepository = new CustomerMariaRepository(connection);
	}

	@AfterClass
	public static void tearDownConnection() throws SQLException {
		if (connection != null) {
			connection.close();
		}
	}

	@Test
	public void testFindAllWhenDatabaseIsEmpty() throws RepositoryException {
		assertThat(customerRepository.findAll())
			.isEmpty();
	}

	@Test
	public void testFindAllWhenDatabaseIsNotEmpty() throws SQLException, RepositoryException {
		addTestCustomerToDatabase(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		addTestCustomerToDatabase(CUSTOMER_ID_2, CUSTOMER_NAME_2);
		assertThat(customerRepository.findAll())
			.containsExactlyInAnyOrder(
					new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1),
					new Customer(CUSTOMER_ID_2, CUSTOMER_NAME_2));
	}

	@Test
	public void testFindAllWhenTableDoesNotExistThrowsRepositoryException() throws SQLException {
		dropCustomerTable();
		assertThatThrownBy(() -> customerRepository.findAll())
			.isInstanceOf(RepositoryException.class)
			.hasCauseInstanceOf(SQLException.class);
	}

	@Test
	public void testFindByIdWhenCustomerDoesNotExist() throws SQLException, RepositoryException {
		addTestCustomerToDatabase(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		assertThat(customerRepository.findById(CUSTOMER_ID_2))
			.isNull();
	}

	@Test
	public void testFindByIdWhenCustomerExists() throws SQLException, RepositoryException {
		addTestCustomerToDatabase(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		addTestCustomerToDatabase(CUSTOMER_ID_2, CUSTOMER_NAME_2);
		assertThat(customerRepository.findById(CUSTOMER_ID_2))
			.isEqualTo(new Customer(CUSTOMER_ID_2, CUSTOMER_NAME_2));
	}

	@Test
	public void testFindByIdWhenTableDoesNotExistThrowsRepositoryException() throws SQLException {
		dropCustomerTable();
		assertThatThrownBy(() -> customerRepository.findById(CUSTOMER_ID_1))
			.isInstanceOf(RepositoryException.class)
			.hasCauseInstanceOf(SQLException.class);
	}

	@Test
	public void testSaveWhenCustomerDoesNotAlreadyExist() throws SQLException, RepositoryException {
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
		assertThatThrownBy(() -> customerRepository.save(customerWithSameId))
			.isInstanceOf(RepositoryException.class);
	}

	@Test
	public void testSaveWhenTableDoesNotExistThrowsRepositoryException() throws SQLException {
		dropCustomerTable();
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		assertThatThrownBy(() -> customerRepository.save(customer))
			.isInstanceOf(RepositoryException.class)
			.hasCauseInstanceOf(SQLException.class);
	}

	@Test
	public void testDeleteWhenCustomerExists() throws SQLException, RepositoryException {
		addTestCustomerToDatabase(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		addTestCustomerToDatabase(CUSTOMER_ID_2, CUSTOMER_NAME_2);
		customerRepository.delete(CUSTOMER_ID_1);
		assertThat(readAllCustomersFromDatabase())
			.containsExactly(
					new Customer(CUSTOMER_ID_2, CUSTOMER_NAME_2));
	}

	@Test
	public void testDeleteWhenCustomerDoesNotExist() throws SQLException, RepositoryException {
		addTestCustomerToDatabase(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		customerRepository.delete(CUSTOMER_ID_2);
		assertThat(readAllCustomersFromDatabase())
			.containsExactly(
					new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1));
	}

	@Test
	public void testDeleteWhenTableDoesNotExistThrowsRepositoryException() throws SQLException {
		dropCustomerTable();
		assertThatThrownBy(() -> customerRepository.delete(CUSTOMER_ID_1))
			.isInstanceOf(RepositoryException.class)
			.hasCauseInstanceOf(SQLException.class);
	}

	private void addTestCustomerToDatabase(String id, String name) throws SQLException {
		String query =
				"INSERT INTO " + CustomerMariaRepository.CUSTOMER_TABLE_NAME + " ("
				+ CustomerMariaRepository.CUSTOMER_ID_KEY + ", "
				+ CustomerMariaRepository.CUSTOMER_NAME_KEY + ") "
				+ "VALUES (?, ?)";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, id);
			statement.setString(2, name);
			statement.executeUpdate();
		}
	}

	private List<Customer> readAllCustomersFromDatabase() throws SQLException {
		List<Customer> customers = new ArrayList<>();
		String query =
				"SELECT "
				+ CustomerMariaRepository.CUSTOMER_ID_KEY + ", "
				+ CustomerMariaRepository.CUSTOMER_NAME_KEY
				+ " FROM "
				+ CustomerMariaRepository.CUSTOMER_TABLE_NAME;
		try (PreparedStatement statement = connection.prepareStatement(query);
				ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				customers.add(
						new Customer(
								resultSet.getString(CustomerMariaRepository.CUSTOMER_ID_KEY),
								resultSet.getString(CustomerMariaRepository.CUSTOMER_NAME_KEY)));
			}
		}
		return customers;
	}

	private void dropCustomerTable() throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute("DROP TABLE " + CustomerMariaRepository.CUSTOMER_TABLE_NAME);
		}
	}
}
