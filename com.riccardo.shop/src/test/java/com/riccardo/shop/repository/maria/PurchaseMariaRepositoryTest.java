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

import com.riccardo.shop.model.Purchase;
import com.riccardo.shop.repository.RepositoryException;

public class PurchaseMariaRepositoryTest {

	private static final String MARIADB_IMAGE = "mariadb:11";

	private static final String CUSTOMER_ID_1 = "C1";
	private static final String CUSTOMER_ID_2 = "C2";

	private static final String PRODUCT_ID_1 = "P1";
	private static final String PRODUCT_ID_2 = "P2";

	@ClassRule
	public static final MariaDBContainer<?> mariadb = new MariaDBContainer<>(MARIADB_IMAGE);

	private static Connection connection;
	private PurchaseMariaRepository purchaseRepository;

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
			statement.execute("DROP TABLE IF EXISTS " + PurchaseMariaRepository.PURCHASE_TABLE_NAME);
			statement.execute("DROP TABLE IF EXISTS " + CustomerMariaRepository.CUSTOMER_TABLE_NAME);
			statement.execute("DROP TABLE IF EXISTS " + ProductMariaRepository.PRODUCT_TABLE_NAME);
			statement.execute(
					"CREATE TABLE " + CustomerMariaRepository.CUSTOMER_TABLE_NAME + " ("
					+ CustomerMariaRepository.CUSTOMER_ID_KEY + " VARCHAR(255) PRIMARY KEY, "
					+ CustomerMariaRepository.CUSTOMER_NAME_KEY + " VARCHAR(255) NOT NULL)");
			statement.execute(
					"CREATE TABLE " + ProductMariaRepository.PRODUCT_TABLE_NAME + " ("
					+ ProductMariaRepository.PRODUCT_ID_KEY + " VARCHAR(255) PRIMARY KEY, "
					+ ProductMariaRepository.PRODUCT_NAME_KEY + " VARCHAR(255) NOT NULL, "
					+ ProductMariaRepository.PRODUCT_PRICE_KEY + " DOUBLE NOT NULL)");
			statement.execute(
					"CREATE TABLE " + PurchaseMariaRepository.PURCHASE_TABLE_NAME + " ("
					+ PurchaseMariaRepository.CUSTOMER_ID_KEY + " VARCHAR(255), "
					+ PurchaseMariaRepository.PRODUCT_ID_KEY + " VARCHAR(255), "
					+ "PRIMARY KEY ("
					+ PurchaseMariaRepository.CUSTOMER_ID_KEY + ", "
					+ PurchaseMariaRepository.PRODUCT_ID_KEY + "), "
					+ "FOREIGN KEY ("
					+ PurchaseMariaRepository.CUSTOMER_ID_KEY + ") REFERENCES "
					+ CustomerMariaRepository.CUSTOMER_TABLE_NAME + "("
					+ CustomerMariaRepository.CUSTOMER_ID_KEY + ") "
					+ "ON DELETE RESTRICT, "
					+ "FOREIGN KEY ("
					+ PurchaseMariaRepository.PRODUCT_ID_KEY + ") REFERENCES "
					+ ProductMariaRepository.PRODUCT_TABLE_NAME + "("
					+ ProductMariaRepository.PRODUCT_ID_KEY + ") "
					+ "ON DELETE RESTRICT)");
		}
		purchaseRepository = new PurchaseMariaRepository(connection);
	}

	@AfterClass
	public static void tearDownConnection() throws SQLException {
		if (connection != null) {
			connection.close();
		}
	}

	@Test
	public void testFindAllWhenDatabaseIsEmpty() throws RepositoryException {
		assertThat(purchaseRepository.findAll())
			.isEmpty();
	}

	@Test
	public void testFindAllWhenDatabaseIsNotEmpty() throws SQLException, RepositoryException {
		addTestCustomerToDatabase(CUSTOMER_ID_1);
		addTestCustomerToDatabase(CUSTOMER_ID_2);
		addTestProductToDatabase(PRODUCT_ID_1);
		addTestProductToDatabase(PRODUCT_ID_2);
		addTestPurchaseToDatabase(CUSTOMER_ID_1, PRODUCT_ID_1);
		addTestPurchaseToDatabase(CUSTOMER_ID_2, PRODUCT_ID_2);
		assertThat(purchaseRepository.findAll())
			.containsExactlyInAnyOrder(
					new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1),
					new Purchase(CUSTOMER_ID_2, PRODUCT_ID_2));
	}

	@Test
	public void testFindAllWhenTableDoesNotExistThrowsRepositoryException() throws SQLException {
		dropPurchaseTable();
		assertThatThrownBy(() -> purchaseRepository.findAll())
			.isInstanceOf(RepositoryException.class)
			.hasCauseInstanceOf(SQLException.class);
	}

	@Test
	public void testFindByCustomerIdWhenPurchasesDoNotExist() throws SQLException, RepositoryException {
		addTestCustomerToDatabase(CUSTOMER_ID_1);
		addTestProductToDatabase(PRODUCT_ID_1);
		addTestProductToDatabase(PRODUCT_ID_2);
		addTestPurchaseToDatabase(CUSTOMER_ID_1, PRODUCT_ID_1);
		addTestPurchaseToDatabase(CUSTOMER_ID_1, PRODUCT_ID_2);
		assertThat(purchaseRepository.findByCustomerId(CUSTOMER_ID_2))
			.isEmpty();
	}

	@Test
	public void testFindByCustomerIdWhenPurchasesExist() throws SQLException, RepositoryException {
		addTestCustomerToDatabase(CUSTOMER_ID_1);
		addTestCustomerToDatabase(CUSTOMER_ID_2);
		addTestProductToDatabase(PRODUCT_ID_1);
		addTestProductToDatabase(PRODUCT_ID_2);
		addTestPurchaseToDatabase(CUSTOMER_ID_1, PRODUCT_ID_1);
		addTestPurchaseToDatabase(CUSTOMER_ID_1, PRODUCT_ID_2);
		addTestPurchaseToDatabase(CUSTOMER_ID_2, PRODUCT_ID_1);
		assertThat(purchaseRepository.findByCustomerId(CUSTOMER_ID_1))
			.containsExactlyInAnyOrder(
					new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1),
					new Purchase(CUSTOMER_ID_1, PRODUCT_ID_2));
	}

	@Test
	public void testFindByCustomerIdWhenTableDoesNotExistThrowsRepositoryException() throws SQLException {
		dropPurchaseTable();
		assertThatThrownBy(
				() -> purchaseRepository.findByCustomerId(CUSTOMER_ID_1))
			.isInstanceOf(RepositoryException.class)
			.hasCauseInstanceOf(SQLException.class);
	}

	@Test
	public void testFindByProductIdWhenPurchasesDoNotExist() throws SQLException, RepositoryException {
		addTestCustomerToDatabase(CUSTOMER_ID_1);
		addTestCustomerToDatabase(CUSTOMER_ID_2);
		addTestProductToDatabase(PRODUCT_ID_1);
		addTestPurchaseToDatabase(CUSTOMER_ID_1, PRODUCT_ID_1);
		addTestPurchaseToDatabase(CUSTOMER_ID_2, PRODUCT_ID_1);
		assertThat(purchaseRepository.findByProductId(PRODUCT_ID_2))
			.isEmpty();
	}

	@Test
	public void testFindByProductIdWhenPurchasesExist() throws SQLException, RepositoryException {
		addTestCustomerToDatabase(CUSTOMER_ID_1);
		addTestCustomerToDatabase(CUSTOMER_ID_2);
		addTestProductToDatabase(PRODUCT_ID_1);
		addTestProductToDatabase(PRODUCT_ID_2);
		addTestPurchaseToDatabase(CUSTOMER_ID_1, PRODUCT_ID_1);
		addTestPurchaseToDatabase(CUSTOMER_ID_2, PRODUCT_ID_1);
		addTestPurchaseToDatabase(CUSTOMER_ID_1, PRODUCT_ID_2);
		assertThat(purchaseRepository.findByProductId(PRODUCT_ID_1))
			.containsExactlyInAnyOrder(
					new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1),
					new Purchase(CUSTOMER_ID_2, PRODUCT_ID_1));
	}

	@Test
	public void testFindByProductIdWhenTableDoesNotExistThrowsRepositoryException() throws SQLException {
		dropPurchaseTable();
		assertThatThrownBy(
				() -> purchaseRepository.findByProductId(PRODUCT_ID_1))
			.isInstanceOf(RepositoryException.class)
			.hasCauseInstanceOf(SQLException.class);
	}

	@Test
	public void testFindByCustomerIdAndProductIdWhenPurchaseDoesNotExist() throws SQLException, RepositoryException {
		addTestCustomerToDatabase(CUSTOMER_ID_1);
		addTestCustomerToDatabase(CUSTOMER_ID_2);
		addTestProductToDatabase(PRODUCT_ID_1);
		addTestProductToDatabase(PRODUCT_ID_2);
		addTestPurchaseToDatabase(CUSTOMER_ID_2, PRODUCT_ID_1);
		addTestPurchaseToDatabase(CUSTOMER_ID_1, PRODUCT_ID_2);
		addTestPurchaseToDatabase(CUSTOMER_ID_2, PRODUCT_ID_2);
		assertThat(
				purchaseRepository.findByCustomerIdAndProductId(CUSTOMER_ID_1, PRODUCT_ID_1))
			.isNull();
	}

	@Test
	public void testFindByCustomerIdAndProductIdWhenPurchaseExists() throws SQLException, RepositoryException {
		addTestCustomerToDatabase(CUSTOMER_ID_1);
		addTestProductToDatabase(PRODUCT_ID_1);
		addTestPurchaseToDatabase(CUSTOMER_ID_1, PRODUCT_ID_1);
		assertThat(
				purchaseRepository.findByCustomerIdAndProductId(CUSTOMER_ID_1, PRODUCT_ID_1))
			.isEqualTo(
					new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1));
	}

	@Test
	public void testFindByCustomerIdAndProductIdWhenTableDoesNotExistThrowsRepositoryException() throws SQLException {
		dropPurchaseTable();
		assertThatThrownBy(
				() -> purchaseRepository.findByCustomerIdAndProductId(
						CUSTOMER_ID_1,
						PRODUCT_ID_1))
			.isInstanceOf(RepositoryException.class)
			.hasCauseInstanceOf(SQLException.class);
	}

	@Test
	public void testSaveWhenPurchaseDoesNotAlreadyExist() throws SQLException, RepositoryException {
		addTestCustomerToDatabase(CUSTOMER_ID_1);
		addTestProductToDatabase(PRODUCT_ID_1);
		Purchase purchase = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		purchaseRepository.save(purchase);
		assertThat(readAllPurchasesFromDatabase())
			.containsExactly(purchase);
	}

	@Test
	public void testSaveWhenPurchaseAlreadyExistsThrowsRepositoryException() throws SQLException, RepositoryException {
		addTestCustomerToDatabase(CUSTOMER_ID_1);
		addTestProductToDatabase(PRODUCT_ID_1);
		Purchase purchase = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		Purchase purchaseWithSameIds = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		purchaseRepository.save(purchase);
		assertThatThrownBy(() -> purchaseRepository.save(purchaseWithSameIds))
			.isInstanceOf(RepositoryException.class);
	}

	@Test
	public void testSaveWhenCustomerDoesNotExistThrowsRepositoryException() throws SQLException {
		addTestProductToDatabase(PRODUCT_ID_1);
		Purchase purchase = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		assertThatThrownBy(() -> purchaseRepository.save(purchase))
			.isInstanceOf(RepositoryException.class);
	}

	@Test
	public void testSaveWhenProductDoesNotExistThrowsRepositoryException() throws SQLException {
		addTestCustomerToDatabase(CUSTOMER_ID_1);
		Purchase purchase = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		assertThatThrownBy(() -> purchaseRepository.save(purchase))
			.isInstanceOf(RepositoryException.class);
	}

	@Test
	public void testSaveWhenTableDoesNotExistThrowsRepositoryException() throws SQLException {
		dropPurchaseTable();
		Purchase purchase = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		assertThatThrownBy(() -> purchaseRepository.save(purchase))
			.isInstanceOf(RepositoryException.class)
			.hasCauseInstanceOf(SQLException.class);
	}

	@Test
	public void testDeleteWhenPurchaseExists() throws SQLException, RepositoryException {
		addTestCustomerToDatabase(CUSTOMER_ID_1);
		addTestProductToDatabase(PRODUCT_ID_1);
		addTestProductToDatabase(PRODUCT_ID_2);
		addTestPurchaseToDatabase(CUSTOMER_ID_1, PRODUCT_ID_1);
		addTestPurchaseToDatabase(CUSTOMER_ID_1, PRODUCT_ID_2);
		purchaseRepository.delete(CUSTOMER_ID_1, PRODUCT_ID_1);
		assertThat(readAllPurchasesFromDatabase())
			.containsExactly(
					new Purchase(CUSTOMER_ID_1, PRODUCT_ID_2));
	}

	@Test
	public void testDeleteWhenPurchaseDoesNotExist() throws SQLException, RepositoryException {
		addTestCustomerToDatabase(CUSTOMER_ID_1);
		addTestProductToDatabase(PRODUCT_ID_1);
		addTestPurchaseToDatabase(CUSTOMER_ID_1, PRODUCT_ID_1);
		purchaseRepository.delete(CUSTOMER_ID_1, PRODUCT_ID_2);
		assertThat(readAllPurchasesFromDatabase())
			.containsExactly(
					new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1));
	}

	@Test
	public void testDeleteWhenTableDoesNotExistThrowsRepositoryException() throws SQLException {
		dropPurchaseTable();
		assertThatThrownBy(
				() -> purchaseRepository.delete(
						CUSTOMER_ID_1,
						PRODUCT_ID_1))
			.isInstanceOf(RepositoryException.class)
			.hasCauseInstanceOf(SQLException.class);
	}

	private void addTestCustomerToDatabase(String id) throws SQLException {
		String query =
				"INSERT INTO "
				+ CustomerMariaRepository.CUSTOMER_TABLE_NAME + " ("
				+ CustomerMariaRepository.CUSTOMER_ID_KEY + ", "
				+ CustomerMariaRepository.CUSTOMER_NAME_KEY + ") "
				+ "VALUES (?, ?)";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, id);
			statement.setString(2, "customer_" + id);
			statement.executeUpdate();
		}
	}

	private void addTestProductToDatabase(String id) throws SQLException {
		String query =
				"INSERT INTO "
				+ ProductMariaRepository.PRODUCT_TABLE_NAME + " ("
				+ ProductMariaRepository.PRODUCT_ID_KEY + ", "
				+ ProductMariaRepository.PRODUCT_NAME_KEY + ", "
				+ ProductMariaRepository.PRODUCT_PRICE_KEY + ") "
				+ "VALUES (?, ?, ?)";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, id);
			statement.setString(2, "product_" + id);
			statement.setDouble(3, 10.0);
			statement.executeUpdate();
		}
	}

	private void addTestPurchaseToDatabase(String customerId, String productId) throws SQLException {
		String query =
				"INSERT INTO " + PurchaseMariaRepository.PURCHASE_TABLE_NAME + " ("
				+ PurchaseMariaRepository.CUSTOMER_ID_KEY + ", "
				+ PurchaseMariaRepository.PRODUCT_ID_KEY + ") "
				+ "VALUES (?, ?)";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, customerId);
			statement.setString(2, productId);
			statement.executeUpdate();
		}
	}

	private List<Purchase> readAllPurchasesFromDatabase() throws SQLException {
		List<Purchase> purchases = new ArrayList<>();
		String query =
				"SELECT "
				+ PurchaseMariaRepository.CUSTOMER_ID_KEY + ", "
				+ PurchaseMariaRepository.PRODUCT_ID_KEY
				+ " FROM "
				+ PurchaseMariaRepository.PURCHASE_TABLE_NAME;
		try (PreparedStatement statement = connection.prepareStatement(query);
				ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				purchases.add(
						new Purchase(
								resultSet.getString(PurchaseMariaRepository.CUSTOMER_ID_KEY),
								resultSet.getString(PurchaseMariaRepository.PRODUCT_ID_KEY)));
			}
		}
		return purchases;
	}

	private void dropPurchaseTable() throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute(
					"DROP TABLE " + PurchaseMariaRepository.PURCHASE_TABLE_NAME);
		}
	}
}
