package com.riccardo.shop.view.swing;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JFrame;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.MariaDBContainer;

import com.riccardo.shop.controller.ProductController;
import com.riccardo.shop.model.Product;
import com.riccardo.shop.repository.ProductRepository;
import com.riccardo.shop.repository.PurchaseRepository;
import com.riccardo.shop.repository.RepositoryException;
import com.riccardo.shop.repository.maria.CustomerMariaRepository;
import com.riccardo.shop.repository.maria.ProductMariaRepository;
import com.riccardo.shop.repository.maria.PurchaseMariaRepository;

@RunWith(GUITestRunner.class)
public class ProductSwingViewMariaRepositoryIT extends AssertJSwingJUnitTestCase {

	private static final String MARIADB_IMAGE = "mariadb:11";

	private static final String PRODUCT_ID_1 = "P1";
	private static final String PRODUCT_NAME_1 = "product1";
	private static final double PRODUCT_PRICE_VALUE_1 = 10.0;
	private static final String PRODUCT_PRICE_STRING_1 = "10.0";

	private static final String PRODUCT_ID_2 = "P2";
	private static final String PRODUCT_NAME_2 = "product2";
	private static final double PRODUCT_PRICE_VALUE_2 = 20.0;

	private static final String DELETE_SELECTED_BUTTON_TEXT = "Delete Selected";
	private static final String ADD_BUTTON_TEXT = "Add";
	private static final String ERROR_MESSAGE_LABEL_NAME = "errorMessageLabel";
	private static final String PRODUCT_LIST_NAME = "productList";
	private static final String PRICE_TEXT_BOX_NAME = "priceTextBox";
	private static final String NAME_TEXT_BOX_NAME = "nameTextBox";
	private static final String ID_TEXT_BOX_NAME = "idTextBox";

	@ClassRule
	public static final MariaDBContainer<?> mariadb = new MariaDBContainer<>(MARIADB_IMAGE);

	private static Connection connection;

	private ProductRepository productRepository;
	private PurchaseRepository purchaseRepository;

	private ProductController productController;
	private ProductSwingView productSwingView;

	private JFrame frame;
	private FrameFixture window;

	@BeforeClass
	public static void setupConnection() throws SQLException {
		connection = DriverManager.getConnection(mariadb.getJdbcUrl(), mariadb.getUsername(), mariadb.getPassword());
	}

	@Override
	protected void onSetUp() throws SQLException {
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
		productRepository = new ProductMariaRepository(connection);
		purchaseRepository = new PurchaseMariaRepository(connection);
		GuiActionRunner.execute(
				() -> {
					productSwingView = new ProductSwingView();
					productController = new ProductController(productSwingView,productRepository,purchaseRepository);
					productSwingView.setProductController(productController);
					frame = new JFrame();
					frame.setContentPane(productSwingView);
					frame.pack();
					return frame;
				}
		);
		window = new FrameFixture(robot(), frame);
		window.show();
	}

	@AfterClass
	public static void tearDownConnection() throws SQLException {
		if (connection != null) {
			connection.close();
		}
	}

	@Test
	@GUITest
	public void testAllProducts() throws RepositoryException {
		Product product1 = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_VALUE_1);
		Product product2 = new Product(PRODUCT_ID_2, PRODUCT_NAME_2, PRODUCT_PRICE_VALUE_2);
		productRepository.save(product1);
		productRepository.save(product2);
		GuiActionRunner.execute(
				() -> productController.allProducts()
		);
		assertThat(window.list(PRODUCT_LIST_NAME).contents()).containsExactly(product1.toString(), product2.toString());
	}

	@Test
	@GUITest
	public void testAddButtonSuccess() {
		Product product = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_VALUE_1);
		window.textBox(ID_TEXT_BOX_NAME).enterText(PRODUCT_ID_1);
		window.textBox(NAME_TEXT_BOX_NAME).enterText(PRODUCT_NAME_1);
		window.textBox(PRICE_TEXT_BOX_NAME).enterText(PRODUCT_PRICE_STRING_1);
		window.button(JButtonMatcher.withText(ADD_BUTTON_TEXT)).click();
		assertThat(window.list(PRODUCT_LIST_NAME).contents()).containsExactly(product.toString());
	}

	@Test
	@GUITest
	public void testAddButtonError() throws RepositoryException {
		Product existingProduct = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_VALUE_1);
		productRepository.save(existingProduct);
		window.textBox(ID_TEXT_BOX_NAME).enterText(PRODUCT_ID_1);
		window.textBox(NAME_TEXT_BOX_NAME).enterText(PRODUCT_NAME_2);
		window.textBox(PRICE_TEXT_BOX_NAME).enterText("20.0");
		window.button(JButtonMatcher.withText(ADD_BUTTON_TEXT)).click();
		assertThat(window.list(PRODUCT_LIST_NAME).contents()).isEmpty();
		window.label(ERROR_MESSAGE_LABEL_NAME).requireText("Already existing product with id " + PRODUCT_ID_1 + ": " + existingProduct);
	}

	@Test
	@GUITest
	public void testDeleteButtonSuccess() {
		Product product = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_VALUE_1);
		GuiActionRunner.execute(
				() -> productController.newProduct(product)
		);
		window.list(PRODUCT_LIST_NAME).selectItem(0);
		window.button(JButtonMatcher.withText(DELETE_SELECTED_BUTTON_TEXT)).click();
		assertThat(window.list(PRODUCT_LIST_NAME).contents()).isEmpty();
	}

	@Test
	@GUITest
	public void testDeleteButtonError() {
		Product product = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_VALUE_1);
		GuiActionRunner.execute(
				() -> productSwingView.getListProductsModel().addElement(product)
		);
		window.list(PRODUCT_LIST_NAME).selectItem(0);
		window.button(JButtonMatcher.withText(DELETE_SELECTED_BUTTON_TEXT)).click();
		assertThat(window.list(PRODUCT_LIST_NAME).contents()).containsExactly(product.toString());
		window.label(ERROR_MESSAGE_LABEL_NAME).requireText("No existing product with id " + PRODUCT_ID_1 + ": " + product);
	}
}