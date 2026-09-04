package com.riccardo.shop.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Arrays;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.riccardo.shop.controller.CustomerController;
import com.riccardo.shop.controller.PurchaseController;
import com.riccardo.shop.model.Customer;
import com.riccardo.shop.model.Product;
import com.riccardo.shop.model.Purchase;

@RunWith(GUITestRunner.class)
public class CustomerPurchaseSwingViewTest extends AssertJSwingJUnitTestCase {

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
	private static final String CUSTOMERS_LABEL_TEXT = "Customers";
	private static final String PURCHASES_LABEL_TEXT = "Purchases";
	private static final String AVAILABLE_PRODUCTS_LABEL_TEXT = "Available Products";
	private static final String NAME_LABEL_TEXT = "name";
	private static final String ID_LABEL_TEXT = "id";
	private static final String ERROR_MESSAGE_LABEL_NAME = "errorMessageLabel";
	private static final String CUSTOMER_LIST_NAME = "customerList";
	private static final String PURCHASE_LIST_NAME = "purchaseList";
	private static final String AVAILABLE_PRODUCT_LIST_NAME = "availableProductList";
	private static final String NAME_TEXT_BOX_NAME = "nameTextBox";
	private static final String ID_TEXT_BOX_NAME = "idTextBox";

	private FrameFixture window;
	private CustomerPurchaseSwingView customerPurchaseSwingView;
	private JFrame frame;
	
	@Mock
	private CustomerController customerController;

	@Mock
	private PurchaseController purchaseController;

	private AutoCloseable closeable;

	@Override
	protected void onSetUp() {
		closeable = MockitoAnnotations.openMocks(this);
		GuiActionRunner.execute(() -> {
			customerPurchaseSwingView = new CustomerPurchaseSwingView();
			customerPurchaseSwingView.setCustomerController(customerController);
			customerPurchaseSwingView.setPurchaseController(purchaseController);
			frame = new JFrame();
			frame.setContentPane(customerPurchaseSwingView);
			frame.pack();
			return frame;
		});
		window = new FrameFixture(robot(), frame);
		window.show();
	}

	@Override
	protected void onTearDown() throws Exception {
		closeable.close();
	}

	@Test
	@GUITest
	public void testControlsInitialStates() {
		window.label(JLabelMatcher.withText(CUSTOMERS_LABEL_TEXT));
		window.label(JLabelMatcher.withText(ID_LABEL_TEXT));
		window.textBox(ID_TEXT_BOX_NAME).requireEnabled();
		window.label(JLabelMatcher.withText(NAME_LABEL_TEXT));
		window.textBox(NAME_TEXT_BOX_NAME).requireEnabled();
		window.button(JButtonMatcher.withText(ADD_BUTTON_TEXT)).requireDisabled();
		window.list(CUSTOMER_LIST_NAME);
		window.button(JButtonMatcher.withText(DELETE_SELECTED_BUTTON_TEXT)).requireDisabled();
		window.label(JLabelMatcher.withText(AVAILABLE_PRODUCTS_LABEL_TEXT));
		window.list(AVAILABLE_PRODUCT_LIST_NAME).requireDisabled();
		window.button(JButtonMatcher.withText(ADD_PURCHASE_BUTTON_TEXT)).requireDisabled();
		window.label(JLabelMatcher.withText(PURCHASES_LABEL_TEXT));
		window.list(PURCHASE_LIST_NAME).requireDisabled();
		window.button(JButtonMatcher.withText(DELETE_PURCHASE_BUTTON_TEXT)).requireDisabled();
		window.label(ERROR_MESSAGE_LABEL_NAME).requireText(" ");
	}

	@Test
	@GUITest
	public void testWhenIdAndNameAreNonEmptyThenAddButtonShouldBeEnabled() {
		window.textBox(ID_TEXT_BOX_NAME).enterText(CUSTOMER_ID_1);
		window.textBox(NAME_TEXT_BOX_NAME).enterText(CUSTOMER_NAME_1);
		window.button(JButtonMatcher.withText(ADD_BUTTON_TEXT)).requireEnabled();
	}

	@Test
	@GUITest
	public void testWhenEitherIdOrNameIsBlankThenAddButtonShouldBeDisabled() {
		JTextComponentFixture idTextBox = window.textBox(ID_TEXT_BOX_NAME);
		JTextComponentFixture nameTextBox = window.textBox(NAME_TEXT_BOX_NAME);
		idTextBox.enterText(CUSTOMER_ID_1);
		nameTextBox.enterText(" ");
		window.button(JButtonMatcher.withText(ADD_BUTTON_TEXT)).requireDisabled();
		idTextBox.setText("");
		nameTextBox.setText("");
		idTextBox.enterText(" ");
		nameTextBox.enterText(CUSTOMER_NAME_1);
		window.button(JButtonMatcher.withText(ADD_BUTTON_TEXT)).requireDisabled();
	}
	
	@Test
	@GUITest
	public void testCustomerControlsShouldBeEnabledOnlyWhenACustomerIsSelected() {
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		GuiActionRunner.execute(
				() -> customerPurchaseSwingView.getListCustomersModel().addElement(customer)
		);
		window.list(CUSTOMER_LIST_NAME).selectItem(0);
		window.button(JButtonMatcher.withText(DELETE_SELECTED_BUTTON_TEXT)).requireEnabled();
		window.list(AVAILABLE_PRODUCT_LIST_NAME).requireEnabled();
		window.list(PURCHASE_LIST_NAME).requireEnabled();
		window.list(CUSTOMER_LIST_NAME).clearSelection();
		window.button(JButtonMatcher.withText(DELETE_SELECTED_BUTTON_TEXT)).requireDisabled();
		window.list(AVAILABLE_PRODUCT_LIST_NAME).requireDisabled();
		window.list(PURCHASE_LIST_NAME).requireDisabled();
	}

	@Test
	@GUITest
	public void testShowAllCustomersShouldAddCustomerDescriptionsToTheList() {
		Customer customer1 = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		Customer customer2 = new Customer(CUSTOMER_ID_2, CUSTOMER_NAME_2);
		GuiActionRunner.execute(
				() -> customerPurchaseSwingView.showAllCustomers(Arrays.asList(customer1, customer2))
		);
		String[] listContents = window.list(CUSTOMER_LIST_NAME).contents();
		assertThat(listContents)
			.containsExactly(
					customer1.toString(),
					customer2.toString()
			);
	}

	@Test
	@GUITest
	public void testShowErrorShouldShowTheMessageInTheErrorLabel() {
		GuiActionRunner.execute(
				() -> customerPurchaseSwingView.showError("error message")
		);
		window.label(ERROR_MESSAGE_LABEL_NAME).requireText("error message");
	}

	@Test
	@GUITest
	public void testShowErrorWithCustomerShouldShowTheMessageInTheErrorLabel() {
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		GuiActionRunner.execute(
				() -> customerPurchaseSwingView.showError("error message", customer)
		);
		window.label(ERROR_MESSAGE_LABEL_NAME).requireText("error message: " + customer);
	}

	@Test
	@GUITest
	public void testShowErrorWithPurchaseShouldShowTheMessageInTheErrorLabel() {
		Purchase purchase = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		GuiActionRunner.execute(
				() -> customerPurchaseSwingView.showError("error message", purchase)
		);
		window.label(ERROR_MESSAGE_LABEL_NAME).requireText("error message: " + purchase);
	}

	@Test
	@GUITest
	public void testCustomerAddedShouldAddTheCustomerToTheListAndResetTheErrorLabel() {
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		GuiActionRunner.execute(
				() -> customerPurchaseSwingView.customerAdded(customer)
		);
		String[] listContents = window.list(CUSTOMER_LIST_NAME).contents();
		assertThat(listContents).containsExactly(customer.toString());
		window.label(ERROR_MESSAGE_LABEL_NAME).requireText(" ");
	}

	@Test
	@GUITest
	public void testCustomerRemovedShouldRemoveTheCustomerFromTheListAndResetTheErrorLabel() {
		Customer customer1 = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		Customer customer2 = new Customer(CUSTOMER_ID_2, CUSTOMER_NAME_2);
		GuiActionRunner.execute(
				() -> {
					DefaultListModel<Customer> listCustomersModel = customerPurchaseSwingView.getListCustomersModel();
					listCustomersModel.addElement(customer1);
					listCustomersModel.addElement(customer2);
				}
		);
		GuiActionRunner.execute(
				() -> customerPurchaseSwingView.customerRemoved(customer1)
		);
		String[] listContents = window.list(CUSTOMER_LIST_NAME).contents();
		assertThat(listContents).containsExactly(customer2.toString());
		window.label(ERROR_MESSAGE_LABEL_NAME).requireText(" ");
	}

	@Test
	@GUITest
	public void testAddButtonShouldDelegateToCustomerControllerNewCustomer() {
		window.textBox(ID_TEXT_BOX_NAME).enterText(CUSTOMER_ID_1);
		window.textBox(NAME_TEXT_BOX_NAME).enterText(CUSTOMER_NAME_1);
		window.button(JButtonMatcher.withText(ADD_BUTTON_TEXT)).click();
		verify(customerController).newCustomer(new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1));
	}

	@Test
	@GUITest
	public void testDeleteButtonShouldDelegateToCustomerControllerDeleteCustomer() {
		Customer customer1 = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		Customer customer2 = new Customer(CUSTOMER_ID_2, CUSTOMER_NAME_2);
		GuiActionRunner.execute(
				() -> {
					DefaultListModel<Customer> listCustomersModel = customerPurchaseSwingView.getListCustomersModel();
					listCustomersModel.addElement(customer1);
					listCustomersModel.addElement(customer2);
				}
		);
		window.list(CUSTOMER_LIST_NAME).selectItem(1);
		window.button(JButtonMatcher.withText(DELETE_SELECTED_BUTTON_TEXT)).click();
		verify(customerController).deleteCustomer(customer2);
	}

	@Test
	@GUITest
	public void testCustomerSelectionShouldDelegateToPurchaseControllerAllCustomerAvailableProducts() {
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		GuiActionRunner.execute(
				() -> customerPurchaseSwingView.getListCustomersModel().addElement(customer)
		);
		window.list(CUSTOMER_LIST_NAME).selectItem(0);
		verify(purchaseController).allCustomerAvailableProducts(customer);
	}

	@Test
	@GUITest
	public void testShowAllCustomerAvailableProductsShouldReplaceProductsInTheList() {
		Product product1 = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		Product product2 = new Product(PRODUCT_ID_2, PRODUCT_NAME_2, PRODUCT_PRICE_2);
		GuiActionRunner.execute(
				() -> {
					customerPurchaseSwingView.showAllCustomerAvailableProducts(Arrays.asList(product1));
					customerPurchaseSwingView.showAllCustomerAvailableProducts(Arrays.asList(product2));
				}
		);
		String[] listContents = window.list(AVAILABLE_PRODUCT_LIST_NAME).contents();
		assertThat(listContents).containsExactly(product2.toString());
	}

	@Test
	@GUITest
	public void testCustomerSelectionShouldDelegateToPurchaseControllerAllCustomerPurchases() {
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		GuiActionRunner.execute(
				() -> customerPurchaseSwingView.getListCustomersModel().addElement(customer)
		);
		window.list(CUSTOMER_LIST_NAME).selectItem(0);
		verify(purchaseController).allCustomerPurchases(customer);
	}

	@Test
	@GUITest
	public void testShowAllCustomerPurchasesShouldReplacePurchasesInTheList() {
		Purchase purchase1 = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		Purchase purchase2 = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_2);
		GuiActionRunner.execute(
				() -> {
					customerPurchaseSwingView.showAllCustomerPurchases(Arrays.asList(purchase1));
					customerPurchaseSwingView.showAllCustomerPurchases(Arrays.asList(purchase2));
				}
		);
		String[] listContents = window.list(PURCHASE_LIST_NAME).contents();
		assertThat(listContents).containsExactly(purchase2.toString());
	}

	@Test
	@GUITest
	public void testAddPurchaseButtonShouldBeEnabledOnlyWhenAnAvailableProductIsSelected() {
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		Product product = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		GuiActionRunner.execute(
				() -> {
					customerPurchaseSwingView.getListCustomersModel().addElement(customer);
					customerPurchaseSwingView.showAllCustomerAvailableProducts(Arrays.asList(product));
				}
		);
		window.list(CUSTOMER_LIST_NAME).selectItem(0);
		window.list(AVAILABLE_PRODUCT_LIST_NAME).selectItem(0);
		window.button(JButtonMatcher.withText(ADD_PURCHASE_BUTTON_TEXT)).requireEnabled();
		window.list(AVAILABLE_PRODUCT_LIST_NAME).clearSelection();
		window.button(JButtonMatcher.withText(ADD_PURCHASE_BUTTON_TEXT)).requireDisabled();
	}

	@Test
	@GUITest
	public void testDeletePurchaseButtonShouldBeEnabledOnlyWhenAPurchaseIsSelected() {
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		Purchase purchase = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		GuiActionRunner.execute(
				() -> {
					customerPurchaseSwingView.getListCustomersModel().addElement(customer);
					customerPurchaseSwingView.showAllCustomerPurchases(Arrays.asList(purchase));
				}
		);
		window.list(CUSTOMER_LIST_NAME).selectItem(0);
		window.list(PURCHASE_LIST_NAME).selectItem(0);
		window.button(JButtonMatcher.withText(DELETE_PURCHASE_BUTTON_TEXT)).requireEnabled();
		window.list(PURCHASE_LIST_NAME).clearSelection();
		window.button(JButtonMatcher.withText(DELETE_PURCHASE_BUTTON_TEXT)).requireDisabled();
	}

	@Test
	@GUITest
	public void testPurchaseButtonsShouldBeDisabledWhenCustomerSelectionIsCleared() {
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		Product product = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		Purchase purchase = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		GuiActionRunner.execute(
				() -> {
					customerPurchaseSwingView.getListCustomersModel().addElement(customer);
					customerPurchaseSwingView.showAllCustomerAvailableProducts(Arrays.asList(product));
					customerPurchaseSwingView.showAllCustomerPurchases(Arrays.asList(purchase));
				}
		);
		window.list(CUSTOMER_LIST_NAME).selectItem(0);
		window.list(AVAILABLE_PRODUCT_LIST_NAME).selectItem(0);
		window.list(PURCHASE_LIST_NAME).selectItem(0);
		window.button(JButtonMatcher.withText(ADD_PURCHASE_BUTTON_TEXT)).requireEnabled();
		window.button(JButtonMatcher.withText(DELETE_PURCHASE_BUTTON_TEXT)).requireEnabled();
		window.list(CUSTOMER_LIST_NAME).clearSelection();
		window.button(JButtonMatcher.withText(ADD_PURCHASE_BUTTON_TEXT)).requireDisabled();
		window.button(JButtonMatcher.withText(DELETE_PURCHASE_BUTTON_TEXT)).requireDisabled();
	}

	@Test
	@GUITest
	public void testAddPurchaseButtonShouldDelegateToPurchaseControllerNewPurchase() {
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		Product product = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_1);
		GuiActionRunner.execute(
				() -> {
					customerPurchaseSwingView.getListCustomersModel().addElement(customer);
					customerPurchaseSwingView.showAllCustomerAvailableProducts(Arrays.asList(product));
				}
		);
		window.list(CUSTOMER_LIST_NAME).selectItem(0);
		window.list(AVAILABLE_PRODUCT_LIST_NAME).selectItem(0);
		window.button(JButtonMatcher.withText(ADD_PURCHASE_BUTTON_TEXT)).click();
		verify(purchaseController).newPurchase(new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1));
	}

	@Test
	@GUITest
	public void testDeletePurchaseButtonShouldDelegateToPurchaseControllerDeletePurchase() {
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		Purchase purchase1 = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		Purchase purchase2 = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_2);
		GuiActionRunner.execute(
				() -> {
					customerPurchaseSwingView.getListCustomersModel().addElement(customer);
					customerPurchaseSwingView.showAllCustomerPurchases(Arrays.asList(purchase1, purchase2));
				}
		);
		window.list(CUSTOMER_LIST_NAME).selectItem(0);
		window.list(PURCHASE_LIST_NAME).selectItem(1);
		window.button(JButtonMatcher.withText(DELETE_PURCHASE_BUTTON_TEXT)).click();
		verify(purchaseController).deletePurchase(purchase2);
	}

	@Test
	@GUITest
	public void testPurchaseAddedShouldRefreshCustomerDataAndResetErrorLabel() {
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		Purchase purchase = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		GuiActionRunner.execute(
				() -> {
					customerPurchaseSwingView.getListCustomersModel().addElement(customer);
					customerPurchaseSwingView.showError("error message");
				}
		);
		window.list(CUSTOMER_LIST_NAME).selectItem(0);
		clearInvocations(purchaseController);
		GuiActionRunner.execute(
				() -> customerPurchaseSwingView.purchaseAdded(purchase)
		);
		verify(purchaseController).allCustomerPurchases(customer);
		verify(purchaseController).allCustomerAvailableProducts(customer);
		window.label(ERROR_MESSAGE_LABEL_NAME).requireText(" ");
	}

	@Test
	@GUITest
	public void testPurchaseRemovedShouldRefreshCustomerDataAndResetErrorLabel() {
		Customer customer = new Customer(CUSTOMER_ID_1, CUSTOMER_NAME_1);
		Purchase purchase = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		GuiActionRunner.execute(
				() -> {
					customerPurchaseSwingView.getListCustomersModel().addElement(customer);
					customerPurchaseSwingView.showError("error message");
				}
		);
		window.list(CUSTOMER_LIST_NAME).selectItem(0);
		clearInvocations(purchaseController);
		GuiActionRunner.execute(
				() -> customerPurchaseSwingView.purchaseRemoved(purchase)
		);
		verify(purchaseController).allCustomerPurchases(customer);
		verify(purchaseController).allCustomerAvailableProducts(customer);
		window.label(ERROR_MESSAGE_LABEL_NAME).requireText(" ");
	}

	@Test
	@GUITest
	public void testPurchaseAddedShouldNotRefreshCustomerDataWhenNoCustomerIsSelected() {
		Purchase purchase = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		GuiActionRunner.execute(
				() -> customerPurchaseSwingView.purchaseAdded(purchase)
		);
		verifyNoInteractions(purchaseController);
	}

	@Test
	@GUITest
	public void testPurchaseRemovedShouldNotRefreshCustomerDataWhenNoCustomerIsSelected() {
		Purchase purchase = new Purchase(CUSTOMER_ID_1, PRODUCT_ID_1);
		GuiActionRunner.execute(
				() -> customerPurchaseSwingView.purchaseRemoved(purchase)
		);
		verifyNoInteractions(purchaseController);
	}
}
