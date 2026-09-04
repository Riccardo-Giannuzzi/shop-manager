package com.riccardo.shop.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.riccardo.shop.controller.ProductController;
import com.riccardo.shop.model.Product;

@RunWith(GUITestRunner.class)
public class ProductSwingViewTest extends AssertJSwingJUnitTestCase {

	private static final String PRODUCT_ID_1 = "P1";
	private static final String PRODUCT_NAME_1 = "product1";
	private static final double PRODUCT_PRICE_VALUE_1 = 10.0;
	private static final String PRODUCT_PRICE_STRING_1 = "10.0";

	private static final String PRODUCT_ID_2 = "P2";
	private static final String PRODUCT_NAME_2 = "product2";
	private static final double PRODUCT_PRICE_VALUE_2 = 20.0;

	private static final String INVALID_PRICE = "notanumber";

	private static final String DELETE_SELECTED_BUTTON_TEXT = "Delete Selected";
	private static final String ADD_BUTTON_TEXT = "Add";
	private static final String PRICE_LABEL_TEXT = "price";
	private static final String NAME_LABEL_TEXT = "name";
	private static final String ID_LABEL_TEXT = "id";
	private static final String ERROR_MESSAGE_LABEL_NAME = "errorMessageLabel";
	private static final String PRODUCT_LIST_NAME = "productList";
	private static final String PRICE_TEXT_BOX_NAME = "priceTextBox";
	private static final String NAME_TEXT_BOX_NAME = "nameTextBox";
	private static final String ID_TEXT_BOX_NAME = "idTextBox";

	private FrameFixture window;
	private ProductSwingView productSwingView;
	private JFrame frame;
	
	@Mock
	private ProductController productController;

	private AutoCloseable closeable;

	@Override
	protected void onSetUp() {
		closeable = MockitoAnnotations.openMocks(this);
		GuiActionRunner.execute(() -> {
			productSwingView = new ProductSwingView();
			productSwingView.setProductController(productController);
			frame = new JFrame();
			frame.setContentPane(productSwingView);
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

	@Test @GUITest
	public void testControlsInitialStates() {
		window.label(JLabelMatcher.withText(ID_LABEL_TEXT));
		window.textBox(ID_TEXT_BOX_NAME).requireEnabled();
		window.label(JLabelMatcher.withText(NAME_LABEL_TEXT));
		window.textBox(NAME_TEXT_BOX_NAME).requireEnabled();
		window.label(JLabelMatcher.withText(PRICE_LABEL_TEXT));
		window.textBox(PRICE_TEXT_BOX_NAME).requireEnabled();
		window.button(JButtonMatcher.withText(ADD_BUTTON_TEXT)).requireDisabled();
		window.list(PRODUCT_LIST_NAME);
		window.button(JButtonMatcher.withText(DELETE_SELECTED_BUTTON_TEXT)).requireDisabled();
		window.label(ERROR_MESSAGE_LABEL_NAME).requireText(" ");
	}

	@Test
	public void testWhenIdNameAndPriceAreNonEmptyThenAddButtonShouldBeEnabled() {
		window.textBox(ID_TEXT_BOX_NAME).enterText(PRODUCT_ID_1);
		window.textBox(NAME_TEXT_BOX_NAME).enterText(PRODUCT_NAME_1);
		window.textBox(PRICE_TEXT_BOX_NAME).enterText(PRODUCT_PRICE_STRING_1);
		window.button(JButtonMatcher.withText(ADD_BUTTON_TEXT)).requireEnabled();
	}

	@Test
	public void testWhenEitherIdNameOrPriceAreBlankThenAddButtonShouldBeDisabled() {
		JTextComponentFixture idTextBox = window.textBox(ID_TEXT_BOX_NAME);
		JTextComponentFixture nameTextBox = window.textBox(NAME_TEXT_BOX_NAME);
		JTextComponentFixture priceTextBox = window.textBox(PRICE_TEXT_BOX_NAME);
		idTextBox.enterText(PRODUCT_ID_1);
		nameTextBox.enterText(PRODUCT_NAME_1);
		priceTextBox.enterText(" ");
		window.button(JButtonMatcher.withText(ADD_BUTTON_TEXT)).requireDisabled();
		idTextBox.setText("");
		nameTextBox.setText("");
		priceTextBox.setText("");
		idTextBox.enterText(PRODUCT_ID_1);
		nameTextBox.enterText(" ");
		priceTextBox.enterText(PRODUCT_PRICE_STRING_1);
		window.button(JButtonMatcher.withText(ADD_BUTTON_TEXT)).requireDisabled();
		idTextBox.setText("");
		nameTextBox.setText("");
		priceTextBox.setText("");
		idTextBox.enterText(" ");
		nameTextBox.enterText(PRODUCT_NAME_1);
		priceTextBox.enterText(PRODUCT_PRICE_STRING_1);
		window.button(JButtonMatcher.withText(ADD_BUTTON_TEXT)).requireDisabled();
	}

	@Test
	public void testWhenPriceIsNotANumberThenAddButtonShouldBeDisabled() {
		window.textBox(ID_TEXT_BOX_NAME).enterText(PRODUCT_ID_1);
		window.textBox(NAME_TEXT_BOX_NAME).enterText(PRODUCT_NAME_1);
		window.textBox(PRICE_TEXT_BOX_NAME).enterText(INVALID_PRICE);
		window.button(JButtonMatcher.withText(ADD_BUTTON_TEXT)).requireDisabled();
	}

	@Test
	public void testDeleteButtonShouldBeEnabledOnlyWhenAProductIsSelected() {
		GuiActionRunner.execute(() -> productSwingView.getListProductsModel().addElement(new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_VALUE_1)));
		window.list(PRODUCT_LIST_NAME).selectItem(0);
		JButtonFixture deleteButton = window.button(JButtonMatcher.withText(DELETE_SELECTED_BUTTON_TEXT));
		deleteButton.requireEnabled();
		window.list(PRODUCT_LIST_NAME).clearSelection();
		deleteButton.requireDisabled();
	}

	@Test
	public void testShowAllProductsShouldAddProductDescriptionsToTheList() {
		Product product1 = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_VALUE_1);
		Product product2 = new Product(PRODUCT_ID_2, PRODUCT_NAME_2, PRODUCT_PRICE_VALUE_2);
		GuiActionRunner.execute(
				() -> productSwingView.showAllProducts(
						Arrays.asList(product1, product2))
		);
		String[] listContents = window.list(PRODUCT_LIST_NAME).contents();
		assertThat(listContents)
			.containsExactly(product1.toString(), product2.toString());
	}

	@Test
	public void testShowErrorShouldShowTheMessageInTheErrorLabel() {
		Product product = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_VALUE_1);
		GuiActionRunner.execute(
				() -> productSwingView.showError("error message", product)
		);
		window.label(ERROR_MESSAGE_LABEL_NAME).requireText("error message: " + product);
	}

	@Test
	public void testShowErrorShouldShowTheMessageInTheErrorLabelWithoutProduct() {
		GuiActionRunner.execute(
				() -> productSwingView.showError("error message")
		);
		window.label(ERROR_MESSAGE_LABEL_NAME).requireText("error message");
	}

	@Test
	public void testProductAddedShouldAddTheProductToTheListAndResetTheErrorLabel() {
		Product product = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_VALUE_1);
		GuiActionRunner.execute(
				() -> productSwingView.productAdded(product)
		);
		String[] listContents = window.list(PRODUCT_LIST_NAME).contents();
		assertThat(listContents).containsExactly(product.toString());
		window.label(ERROR_MESSAGE_LABEL_NAME).requireText(" ");
	}

	@Test
	public void testProductRemovedShouldRemoveTheProductFromTheListAndResetTheErrorLabel() {
		Product product1 = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_VALUE_1);
		Product product2 = new Product(PRODUCT_ID_2, PRODUCT_NAME_2, PRODUCT_PRICE_VALUE_2);
		GuiActionRunner.execute(
				() -> {
					DefaultListModel<Product> listProductsModel = productSwingView.getListProductsModel();
					listProductsModel.addElement(product1);
					listProductsModel.addElement(product2);
				}
		);
		GuiActionRunner.execute(
				() -> productSwingView.productRemoved(product1)
		);
		String[] listContents = window.list(PRODUCT_LIST_NAME).contents();
		assertThat(listContents).containsExactly(product2.toString());
		window.label(ERROR_MESSAGE_LABEL_NAME).requireText(" ");
	}

	@Test
	public void testAddButtonShouldDelegateToProductControllerNewProduct() {
		window.textBox(ID_TEXT_BOX_NAME).enterText(PRODUCT_ID_1);
		window.textBox(NAME_TEXT_BOX_NAME).enterText(PRODUCT_NAME_1);
		window.textBox(PRICE_TEXT_BOX_NAME).enterText(PRODUCT_PRICE_STRING_1);
		window.button(JButtonMatcher.withText(ADD_BUTTON_TEXT)).click();
		verify(productController).newProduct(new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_VALUE_1));
	}

	@Test
	public void testDeleteButtonShouldDelegateToProductControllerDeleteProduct() {
		Product product1 = new Product(PRODUCT_ID_1, PRODUCT_NAME_1, PRODUCT_PRICE_VALUE_1);
		Product product2 = new Product(PRODUCT_ID_2, PRODUCT_NAME_2, PRODUCT_PRICE_VALUE_2);
		GuiActionRunner.execute(
				() -> {
					DefaultListModel<Product> listProductsModel = productSwingView.getListProductsModel();
					listProductsModel.addElement(product1);
					listProductsModel.addElement(product2);
				}
		);
		window.list(PRODUCT_LIST_NAME).selectItem(1);
		window.button(JButtonMatcher.withText(DELETE_SELECTED_BUTTON_TEXT)).click();
		verify(productController).deleteProduct(product2);
	}
}
