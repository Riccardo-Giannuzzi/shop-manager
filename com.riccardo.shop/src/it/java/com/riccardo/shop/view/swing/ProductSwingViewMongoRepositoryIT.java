package com.riccardo.shop.view.swing;

import static org.assertj.core.api.Assertions.assertThat;

import javax.swing.JFrame;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.MongoDBContainer;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import com.riccardo.shop.controller.ProductController;
import com.riccardo.shop.model.Product;
import com.riccardo.shop.repository.ProductRepository;
import com.riccardo.shop.repository.PurchaseRepository;
import com.riccardo.shop.repository.RepositoryException;
import com.riccardo.shop.repository.mongo.ProductMongoRepository;
import com.riccardo.shop.repository.mongo.PurchaseMongoRepository;

@RunWith(GUITestRunner.class)
public class ProductSwingViewMongoRepositoryIT extends AssertJSwingJUnitTestCase {

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

	public static final String SHOP_DB_NAME = "shop";
	public static final String PRODUCT_COLLECTION_NAME = "product";
	public static final String PURCHASE_COLLECTION_NAME = "purchase";

	@ClassRule
	public static final MongoDBContainer mongo =
			new MongoDBContainer("mongo:5");

	private MongoClient client;

	private ProductRepository productRepository;
	private PurchaseRepository purchaseRepository;

	private ProductController productController;
	private ProductSwingView productSwingView;

	private JFrame frame;
	private FrameFixture window;

	@Override
	protected void onSetUp() {
		client = new MongoClient(new ServerAddress(mongo.getHost(), mongo.getFirstMappedPort()));
		MongoDatabase database = client.getDatabase(SHOP_DB_NAME);
		database.drop();
		productRepository = new ProductMongoRepository(client, SHOP_DB_NAME, PRODUCT_COLLECTION_NAME);
		purchaseRepository = new PurchaseMongoRepository(client,SHOP_DB_NAME, PURCHASE_COLLECTION_NAME);
		GuiActionRunner.execute(
				() -> {
					productSwingView = new ProductSwingView();
					productController = new ProductController(productSwingView, productRepository, purchaseRepository);
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

	@Override
	protected void onTearDown() {
		client.close();
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
		assertThat(window.list(PRODUCT_LIST_NAME).contents()).containsExactly(product1.toString(),product2.toString());
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
		window.label(ERROR_MESSAGE_LABEL_NAME).requireText("No existing product with id "+ PRODUCT_ID_1+ ": "+ product);
	}
}