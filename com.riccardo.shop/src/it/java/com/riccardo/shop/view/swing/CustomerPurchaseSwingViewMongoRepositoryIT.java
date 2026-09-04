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
import com.riccardo.shop.controller.CustomerController;
import com.riccardo.shop.controller.PurchaseController;
import com.riccardo.shop.model.Customer;
import com.riccardo.shop.model.Product;
import com.riccardo.shop.model.Purchase;
import com.riccardo.shop.repository.CustomerRepository;
import com.riccardo.shop.repository.ProductRepository;
import com.riccardo.shop.repository.PurchaseRepository;
import com.riccardo.shop.repository.RepositoryException;
import com.riccardo.shop.repository.mongo.CustomerMongoRepository;
import com.riccardo.shop.repository.mongo.ProductMongoRepository;
import com.riccardo.shop.repository.mongo.PurchaseMongoRepository;

@RunWith(GUITestRunner.class)
public class CustomerPurchaseSwingViewMongoRepositoryIT extends AssertJSwingJUnitTestCase {

	private static final String CUSTOMER_ID_1 = "C1";
	private static final String CUSTOMER_NAME_1 = "customer1";

	private static final String CUSTOMER_ID_2 = "C2";
	private static final String CUSTOMER_NAME_2 = "customer2";

	private static final String PRODUCT_ID_1 = "P1";
	private static final String PRODUCT_NAME_1 = "product1";
	private static final double PRODUCT_PRICE_1 = 10.0;

	private static final String PRODUCT_ID_2 = "P2";
	private static final String PRODUCT_NAME_2 = "product2";
	private static final double PRODUCT_PRICE_2 = 20.0;

	private static final String DELETE_SELECTED_BUTTON_TEXT = "Delete Customer";
	private static final String ADD_BUTTON_TEXT = "Add";
	private static final String ADD_PURCHASE_BUTTON_TEXT = "Add Purchase";
	private static final String DELETE_PURCHASE_BUTTON_TEXT = "Delete Purchase";
	private static final String ERROR_MESSAGE_LABEL_NAME = "errorMessageLabel";
	private static final String CUSTOMER_LIST_NAME = "customerList";
	private static final String PURCHASE_LIST_NAME = "purchaseList";
	private static final String AVAILABLE_PRODUCT_LIST_NAME = "availableProductList";
	private static final String NAME_TEXT_BOX_NAME = "nameTextBox";
	private static final String ID_TEXT_BOX_NAME = "idTextBox";

	@ClassRule
	public static final MongoDBContainer mongo = new MongoDBContainer("mongo:5");

	private MongoClient client;

	private CustomerRepository customerRepository;
	private ProductRepository productRepository;
	private PurchaseRepository purchaseRepository;

	private CustomerController customerController;
	private PurchaseController purchaseController;

	private CustomerPurchaseSwingView customerPurchaseSwingView;

	private JFrame frame;
	private FrameFixture window;

	@Override
	protected void onSetUp() {
		client = new MongoClient(new ServerAddress(mongo.getHost(),mongo.getFirstMappedPort()));

		MongoDatabase database = client.getDatabase(CustomerMongoRepository.SHOP_DB_NAME);
		database.drop();
		customerRepository = new CustomerMongoRepository(client);
		productRepository = new ProductMongoRepository(client);
		purchaseRepository = new PurchaseMongoRepository(client);
		GuiActionRunner.execute(
				() -> {
					customerPurchaseSwingView = new CustomerPurchaseSwingView();
					customerController = new CustomerController(customerPurchaseSwingView, customerRepository, purchaseRepository);
					purchaseController = new PurchaseController(customerPurchaseSwingView, purchaseRepository, customerRepository, productRepository);
					customerPurchaseSwingView.setCustomerController(customerController);
					customerPurchaseSwingView.setPurchaseController(purchaseController);
					frame = new JFrame();
					frame.setContentPane(customerPurchaseSwingView);
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
	public void testAllCustomers() throws RepositoryException {
		Customer customer1 = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		Customer customer2 = new Customer(CUSTOMER_ID_2, CUSTOMER_NAME_2);
		customerRepository.save(customer1);
		customerRepository.save(customer2);
		GuiActionRunner.execute(
				() -> customerController.allCustomers()
		);
		assertThat(window.list(CUSTOMER_LIST_NAME).contents()).containsExactly(customer1.toString(),customer2.toString());
	}

	@Test
	@GUITest
	public void testAddCustomerButtonSuccess() {
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		window.textBox(ID_TEXT_BOX_NAME).enterText(CUSTOMER_ID_1);
		window.textBox(NAME_TEXT_BOX_NAME).enterText(CUSTOMER_NAME_1);
		window.button(JButtonMatcher.withText(ADD_BUTTON_TEXT)).click();
		assertThat(window.list(CUSTOMER_LIST_NAME).contents()).containsExactly(customer.toString());
	}

	@Test
	@GUITest
	public void testAddCustomerButtonError() throws RepositoryException {
		Customer existingCustomer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		customerRepository.save(existingCustomer);
		window.textBox(ID_TEXT_BOX_NAME).enterText(CUSTOMER_ID_1);
		window.textBox(NAME_TEXT_BOX_NAME).enterText(CUSTOMER_NAME_2);
		window.button(JButtonMatcher.withText(ADD_BUTTON_TEXT)).click();
		assertThat(window.list(CUSTOMER_LIST_NAME).contents()).isEmpty();
		window.label(ERROR_MESSAGE_LABEL_NAME).requireText("Already existing customer with id " + CUSTOMER_ID_1 + ": " + existingCustomer);
	}

	@Test
	@GUITest
	public void testDeleteCustomerButtonSuccess() {
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		GuiActionRunner.execute(
				() -> customerController.newCustomer(customer)
		);
		window.list(CUSTOMER_LIST_NAME).selectItem(0);
		window.button(JButtonMatcher.withText(DELETE_SELECTED_BUTTON_TEXT)).click();
		assertThat(window.list(CUSTOMER_LIST_NAME).contents()).isEmpty();
	}

	@Test
	@GUITest
	public void testDeleteCustomerButtonError() {
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		GuiActionRunner.execute(
				() -> customerPurchaseSwingView.getListCustomersModel().addElement(customer)
		);
		window.list(CUSTOMER_LIST_NAME).selectItem(0);
		window.button(JButtonMatcher.withText(DELETE_SELECTED_BUTTON_TEXT)).click();
		assertThat(window.list(CUSTOMER_LIST_NAME).contents()).containsExactly(customer.toString());
		window.label(ERROR_MESSAGE_LABEL_NAME).requireText("No existing customer with id " + CUSTOMER_ID_1 + ": " + customer);
	}

	@Test
	@GUITest
	public void testCustomerSelectionShouldShowPurchasesAndAvailableProducts() throws RepositoryException {
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		Product product1 = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		Product product2 = new Product(PRODUCT_ID_2, PRODUCT_NAME_2, PRODUCT_PRICE_2);
		Purchase purchase = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		customerRepository.save(customer);
		productRepository.save(product1);
		productRepository.save(product2);
		purchaseRepository.save(purchase);
		GuiActionRunner.execute(
				() -> customerController.allCustomers()
		);
		window.list(CUSTOMER_LIST_NAME).selectItem(0);
		assertThat(window.list(PURCHASE_LIST_NAME).contents()).containsExactly(purchase.toString());
		assertThat(window.list(AVAILABLE_PRODUCT_LIST_NAME).contents()).containsExactly(product2.toString());
	}

	@Test
	@GUITest
	public void testAddPurchaseButtonSuccess() throws RepositoryException {
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		Product product = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		Purchase purchase = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		customerRepository.save(customer);
		productRepository.save(product);
		GuiActionRunner.execute(
				() -> customerController.allCustomers()
		);
		window.list(CUSTOMER_LIST_NAME).selectItem(0);
		window.list(AVAILABLE_PRODUCT_LIST_NAME).selectItem(0);
		window.button(JButtonMatcher.withText(ADD_PURCHASE_BUTTON_TEXT)).click();
		assertThat(window.list(PURCHASE_LIST_NAME).contents()).containsExactly(purchase.toString());
		assertThat(window.list(AVAILABLE_PRODUCT_LIST_NAME).contents()).isEmpty();
	}

	@Test
	@GUITest
	public void testDeletePurchaseButtonSuccess() throws RepositoryException {
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		Product product = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		Purchase purchase = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		customerRepository.save(customer);
		productRepository.save(product);
		purchaseRepository.save(purchase);
		GuiActionRunner.execute(
				() -> customerController.allCustomers()
		);
		window.list(CUSTOMER_LIST_NAME).selectItem(0);
		window.list(PURCHASE_LIST_NAME).selectItem(0);
		window.button(JButtonMatcher.withText(DELETE_PURCHASE_BUTTON_TEXT)).click();
		assertThat(window.list(PURCHASE_LIST_NAME).contents()).isEmpty();
		assertThat(window.list(AVAILABLE_PRODUCT_LIST_NAME).contents()).containsExactly(product.toString());
	}
}