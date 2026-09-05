package com.riccardo.shop.bdd.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.launcher.ApplicationLauncher.application;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ShopSwingViewSteps {

	private static final String CUSTOMER_LIST_NAME = "customerList";
	private static final String AVAILABLE_PRODUCT_LIST_NAME = "availableProductList";
	private static final String PURCHASE_LIST_NAME = "purchaseList";
	private static final String ERROR_MESSAGE_LABEL_NAME = "errorMessageLabel";
	private static final String ID_TEXT_BOX_NAME = "idTextBox";
	private static final String NAME_TEXT_BOX_NAME = "nameTextBox";

	private FrameFixture window;

	@After
	public void tearDown() {
		if (window != null)
			window.cleanUp();
	}

	@Given("The user provides customer data in the text fields")
	public void the_user_provides_customer_data_in_the_text_fields() {
		window.textBox(ID_TEXT_BOX_NAME).enterText("C10");
		window.textBox(NAME_TEXT_BOX_NAME).enterText("new customer");
	}

	@Given("The user provides customer data in the text fields, specifying an existing id")
	public void the_user_provides_customer_data_in_the_text_fields_specifying_an_existing_id() {
		window.textBox(ID_TEXT_BOX_NAME).enterText(DatabaseSteps.CUSTOMER_FIXTURE_1_ID);
		window.textBox(NAME_TEXT_BOX_NAME).enterText("new customer");
	}

	@Given("The user selects a customer from the list")
	public void the_user_selects_a_customer_from_the_list() {
		window.list(CUSTOMER_LIST_NAME)
			.selectItem(Pattern.compile(".*" + DatabaseSteps.CUSTOMER_FIXTURE_1_NAME + ".*"));
	}

	@Given("The user selects a product from the available products")
	public void the_user_selects_a_product_from_the_available_products() {
		window.list(AVAILABLE_PRODUCT_LIST_NAME)
			.selectItem(Pattern.compile(".*" + DatabaseSteps.PRODUCT_FIXTURE_1_NAME + ".*"));
	}

	@When("The user selects the customer with id {string}")
	public void the_user_selects_the_customer_with_id(String id) {
		window.list(CUSTOMER_LIST_NAME)
			.selectItem(Pattern.compile(".*" + id + ".*"));
	}

	@When("The Customer View is shown")
	public void the_Customer_View_is_shown() {
		application("com.riccardo.shop.app.swing.ShopSwingApp")
			.withArgs("--db-type=mongo", "--mongo-host=localhost", "--mongo-port=27017")
			.start();
		window = WindowFinder.findFrame(
				new GenericTypeMatcher<JFrame>(JFrame.class) {
					@Override
					protected boolean isMatching(JFrame frame) {
						return "Shop Manager".equals(frame.getTitle()) && frame.isShowing();
					}
				})
			.using(BasicRobot.robotWithCurrentAwtHierarchy());
	}

	@When("The user clicks the {string} button")
	public void the_user_clicks_the_button(String buttonText) {
		window.button(JButtonMatcher.withText(buttonText).andShowing()).click();
	}

	@When("The user enters the following values in the text fields")
	public void the_user_enters_the_following_values_in_the_text_fields(List<Map<String, String>> values) {
		values
			.stream()
			.flatMap(m -> m.entrySet().stream())
			.forEach(
					e -> window
						.textBox(e.getKey() + "TextBox")
						.enterText(e.getValue())
			);
	}

	@When("The user selects the product with id {string} from the available products")
	public void the_user_selects_the_product_with_id_from_the_available_products(String id) {
		window.list(AVAILABLE_PRODUCT_LIST_NAME)
			.selectItem(Pattern.compile(".*" + id + ".*"));
	}

	@When("The user selects the purchase with customer id {string} and product id {string}")
	public void the_user_selects_the_purchase_with_customer_id_and_product_id(String customerId, String productId) {
		window.list(PURCHASE_LIST_NAME).selectItem(Pattern.compile(".*" + customerId + ".*" + productId + ".*"));
	}

	@When("The user selects the {string} tab")
	public void the_user_selects_the_tab(String tabName) {
		window.tabbedPane().selectTab(tabName);
	}

	@When("The user selects the product with id {string}")
	public void the_user_selects_the_product_with_id(String id) {
		window.list("productList")
			.selectItem(Pattern.compile(".*" + id + ".*"));
	}

	@Then("The customer list contains elements with the following values")
	public void the_customer_list_contains_elements_with_the_following_values(List<List<String>> values) {
		values.forEach(
				v -> assertThat(window.list(CUSTOMER_LIST_NAME).contents())
					.anySatisfy(e -> assertThat(e).contains(v.get(0), v.get(1)))
		);
	}

	@Then("The customer list is empty")
	public void the_customer_list_is_empty() {
		assertThat(window.list(CUSTOMER_LIST_NAME).contents()).isEmpty();
	}

	@Then("An error is shown containing the following values")
	public void an_error_is_shown_containing_the_following_values(List<List<String>> values) {
		assertThat(window.label(ERROR_MESSAGE_LABEL_NAME).text())
			.contains(values.get(0));
	}

	@Then("The customer list contains the new customer")
	public void the_customer_list_contains_the_new_customer() {
		assertThat(window.list(CUSTOMER_LIST_NAME).contents())
			.anySatisfy(e -> assertThat(e).contains("C10", "new customer"));
	}

	@Then("An error is shown containing the name of the existing customer")
	public void an_error_is_shown_containing_the_name_of_the_existing_customer() {
		assertThat(window.label(ERROR_MESSAGE_LABEL_NAME).text())
			.contains(DatabaseSteps.CUSTOMER_FIXTURE_1_NAME);
	}

	@Then("The customer is removed from the list")
	public void the_customer_is_removed_from_the_list() {
		assertThat(window.list(CUSTOMER_LIST_NAME).contents())
			.noneMatch(e -> e.contains(DatabaseSteps.CUSTOMER_FIXTURE_1_NAME));
	}

	@Then("The purchase list contains the new purchase")
	public void the_purchase_list_contains_the_new_purchase() {
		assertThat(window.list(PURCHASE_LIST_NAME).contents())
			.anySatisfy(e -> assertThat(e).contains(DatabaseSteps.CUSTOMER_FIXTURE_1_ID, DatabaseSteps.PRODUCT_FIXTURE_1_ID));
	}

	@Then("The purchase list contains a purchase with customer id {string} and product id {string}")
	public void the_purchase_list_contains_a_purchase_with_customer_id_and_product_id(String customerId, String productId) {
		assertThat(window.list(PURCHASE_LIST_NAME).contents())
			.anySatisfy(e -> assertThat(e).contains(customerId, productId));
	}

	@Then("The purchase list is empty")
	public void the_purchase_list_is_empty() {
		assertThat(window.list(PURCHASE_LIST_NAME).contents()).isEmpty();
	}

	@Then("The product list contains elements with the following values")
	public void the_product_list_contains_elements_with_the_following_values(List<List<String>> values) {
		values.forEach(
				v -> assertThat(window.list("productList").contents())
					.anySatisfy(e -> assertThat(e).contains(v.get(0), v.get(1), v.get(2)))
		);
	}

	@Then("The product list is empty")
	public void the_product_list_is_empty() {
		assertThat(window.list("productList").contents()).isEmpty();
	}
}