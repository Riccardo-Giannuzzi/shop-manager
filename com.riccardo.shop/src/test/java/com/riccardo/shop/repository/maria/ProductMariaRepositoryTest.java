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

import com.riccardo.shop.model.Product;
import com.riccardo.shop.repository.RepositoryException;

public class ProductMariaRepositoryTest {

	private static final String MARIADB_IMAGE = "mariadb:11";

	private static final String PRODUCT_ID_1 = "P1";
	private static final String PRODUCT_NAME_1 = "product_1";
	private static final double PRODUCT_PRICE_1 = 10.0;

	private static final String PRODUCT_ID_2 = "P2";
	private static final String PRODUCT_NAME_2 = "product_2";
	private static final double PRODUCT_PRICE_2 = 20.0;

	@ClassRule
	public static final MariaDBContainer<?> mariadb = new MariaDBContainer<>(MARIADB_IMAGE);

	private static Connection connection;
	private ProductMariaRepository productRepository;

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
			statement.execute("DROP TABLE IF EXISTS " + ProductMariaRepository.PRODUCT_TABLE_NAME);
			statement.execute(
					"CREATE TABLE " + ProductMariaRepository.PRODUCT_TABLE_NAME + " ("
					+ ProductMariaRepository.PRODUCT_ID_KEY + " VARCHAR(255) PRIMARY KEY, "
					+ ProductMariaRepository.PRODUCT_NAME_KEY + " VARCHAR(255) NOT NULL, "
					+ ProductMariaRepository.PRODUCT_PRICE_KEY + " DOUBLE NOT NULL)");
		}
		productRepository = new ProductMariaRepository(connection);
	}

	@AfterClass
	public static void tearDownConnection() throws SQLException {
		if (connection != null) {
			connection.close();
		}
	}

	@Test
	public void testFindAllWhenDatabaseIsEmpty() throws RepositoryException {
		assertThat(productRepository.findAll())
			.isEmpty();
	}

	@Test
	public void testFindAllWhenDatabaseIsNotEmpty() throws SQLException, RepositoryException {
		addTestProductToDatabase(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		addTestProductToDatabase(PRODUCT_ID_2, PRODUCT_NAME_2, PRODUCT_PRICE_2);
		assertThat(productRepository.findAll())
			.containsExactlyInAnyOrder(
					new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1),
					new Product(PRODUCT_ID_2, PRODUCT_NAME_2, PRODUCT_PRICE_2));
	}

	@Test
	public void testFindAllWhenTableDoesNotExistThrowsRepositoryException() throws SQLException {
		dropProductTable();
		assertThatThrownBy(() -> productRepository.findAll())
			.isInstanceOf(RepositoryException.class)
			.hasCauseInstanceOf(SQLException.class);
	}

	@Test
	public void testFindByIdWhenProductDoesNotExist() throws SQLException, RepositoryException {
		addTestProductToDatabase(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		assertThat(productRepository.findById(PRODUCT_ID_2))
			.isNull();
	}

	@Test
	public void testFindByIdWhenProductExists() throws SQLException, RepositoryException {
		addTestProductToDatabase(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		addTestProductToDatabase(PRODUCT_ID_2, PRODUCT_NAME_2, PRODUCT_PRICE_2);
		assertThat(productRepository.findById(PRODUCT_ID_2))
			.isEqualTo(new Product(PRODUCT_ID_2, PRODUCT_NAME_2, PRODUCT_PRICE_2));
	}

	@Test
	public void testFindByIdWhenTableDoesNotExistThrowsRepositoryException() throws SQLException {
		dropProductTable();
		assertThatThrownBy(() -> productRepository.findById(PRODUCT_ID_1))
			.isInstanceOf(RepositoryException.class)
			.hasCauseInstanceOf(SQLException.class);
	}

	@Test
	public void testSaveWhenProductDoesNotAlreadyExist()
			throws SQLException, RepositoryException {
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
		assertThatThrownBy(() -> productRepository.save(productWithSameId))
			.isInstanceOf(RepositoryException.class);
	}

	@Test
	public void testSaveWhenTableDoesNotExistThrowsRepositoryException() throws SQLException {
		dropProductTable();
		Product product = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		assertThatThrownBy(() -> productRepository.save(product))
			.isInstanceOf(RepositoryException.class)
			.hasCauseInstanceOf(SQLException.class);
	}

	@Test
	public void testDeleteWhenProductExists() throws SQLException, RepositoryException {
		addTestProductToDatabase(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		addTestProductToDatabase(PRODUCT_ID_2, PRODUCT_NAME_2, PRODUCT_PRICE_2);
		productRepository.delete(PRODUCT_ID_1);
		assertThat(readAllProductsFromDatabase())
			.containsExactly(
					new Product(PRODUCT_ID_2, PRODUCT_NAME_2, PRODUCT_PRICE_2));
	}

	@Test
	public void testDeleteWhenProductDoesNotExist() throws SQLException, RepositoryException {
		addTestProductToDatabase(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		productRepository.delete(PRODUCT_ID_2);
		assertThat(readAllProductsFromDatabase())
			.containsExactly(
					new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1));
	}

	@Test
	public void testDeleteWhenTableDoesNotExistThrowsRepositoryException() throws SQLException {
		dropProductTable();
		assertThatThrownBy(() -> productRepository.delete(PRODUCT_ID_1))
			.isInstanceOf(RepositoryException.class)
			.hasCauseInstanceOf(SQLException.class);
	}

	private void addTestProductToDatabase(String id, String name, double price) throws SQLException {
		String query =
				"INSERT INTO " + ProductMariaRepository.PRODUCT_TABLE_NAME + " ("
				+ ProductMariaRepository.PRODUCT_ID_KEY + ", "
				+ ProductMariaRepository.PRODUCT_NAME_KEY + ", "
				+ ProductMariaRepository.PRODUCT_PRICE_KEY + ") "
				+ "VALUES (?, ?, ?)";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, id);
			statement.setString(2, name);
			statement.setDouble(3, price);
			statement.executeUpdate();
		}
	}

	private List<Product> readAllProductsFromDatabase() throws SQLException {
		List<Product> products = new ArrayList<>();
		String query =
				"SELECT "
				+ ProductMariaRepository.PRODUCT_ID_KEY + ", "
				+ ProductMariaRepository.PRODUCT_NAME_KEY + ", "
				+ ProductMariaRepository.PRODUCT_PRICE_KEY
				+ " FROM "
				+ ProductMariaRepository.PRODUCT_TABLE_NAME;
		try (PreparedStatement statement = connection.prepareStatement(query);
				ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				products.add(
						new Product(
								resultSet.getString(ProductMariaRepository.PRODUCT_ID_KEY),
								resultSet.getString(ProductMariaRepository.PRODUCT_NAME_KEY),
								resultSet.getDouble(ProductMariaRepository.PRODUCT_PRICE_KEY)));
			}
		}
		return products;
	}

	private void dropProductTable() throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute("DROP TABLE " + ProductMariaRepository.PRODUCT_TABLE_NAME);
		}
	}
}
