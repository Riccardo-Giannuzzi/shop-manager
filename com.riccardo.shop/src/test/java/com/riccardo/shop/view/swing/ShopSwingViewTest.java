package com.riccardo.shop.view.swing;

import static org.assertj.swing.data.Index.atIndex;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.riccardo.shop.controller.CustomerController;
import com.riccardo.shop.controller.ProductController;
import com.riccardo.shop.controller.PurchaseController;

@RunWith(GUITestRunner.class)
public class ShopSwingViewTest extends AssertJSwingJUnitTestCase {

	private static final String PRODUCTS_TAB_TITLE = "Products";
	private static final String CUSTOMERS_PURCHASES_TAB_TITLE = "Customers Purchases";
	private static final int CUSTOMERS_PURCHASES_TAB_INDEX = 0;
	private static final int PRODUCTS_TAB_INDEX = 1;

	private FrameFixture window;

	private ShopSwingView shopSwingView;

	@Mock
	private ProductController productController;

	@Mock
	private CustomerController customerController;

	@Mock
	private PurchaseController purchaseController;

	private AutoCloseable closeable;

	@Override
	protected void onSetUp() {
		closeable = MockitoAnnotations.openMocks(this);
		GuiActionRunner.execute(
				() -> {
					shopSwingView = new ShopSwingView();
					shopSwingView.setProductController(productController);
					shopSwingView.setCustomerController(customerController);
					shopSwingView.setPurchaseController(purchaseController);
					return shopSwingView;
				}
		);
		window = new FrameFixture(robot(), shopSwingView);
		window.show();
	}

	@Override
	protected void onTearDown() throws Exception {
		closeable.close();
	}

	@Test
	@GUITest
	public void testControlsInitialStates() {
		window.requireTitle("Shop Manager");
		window.tabbedPane().requireTabTitles(CUSTOMERS_PURCHASES_TAB_TITLE, PRODUCTS_TAB_TITLE);
		window.tabbedPane().requireSelectedTab(atIndex(CUSTOMERS_PURCHASES_TAB_INDEX));
	}

	@Test
	@GUITest
	public void testShouldSwitchBetweenTabs() {
		window.tabbedPane().selectTab(PRODUCTS_TAB_INDEX);
		window.tabbedPane().requireSelectedTab(atIndex(PRODUCTS_TAB_INDEX));
		window.tabbedPane().selectTab(CUSTOMERS_PURCHASES_TAB_INDEX);
		window.tabbedPane().requireSelectedTab(atIndex(CUSTOMERS_PURCHASES_TAB_INDEX));
	}

	@Test
	@GUITest
	public void testSelectingProductsTabShouldRefreshProducts() {
		window.tabbedPane().selectTab(PRODUCTS_TAB_INDEX);
		verify(productController).allProducts();
	}

	@Test
	@GUITest
	public void testSelectingCustomersPurchasesTabShouldRefreshCustomers() {
		clearInvocations(customerController);
		window.tabbedPane().selectTab(PRODUCTS_TAB_INDEX);
		window.tabbedPane().selectTab(CUSTOMERS_PURCHASES_TAB_INDEX);
		verify(customerController).allCustomers();
	}

	@Test
	@GUITest
	public void testSetCustomerControllerShouldRefreshCustomers() {
		clearInvocations(customerController);
		GuiActionRunner.execute(
				() -> shopSwingView.setCustomerController(customerController)
		);
		verify(customerController).allCustomers();
	}

	@Test
	@GUITest
	public void testShouldSwitchTabsBeforeControllersAreSet() {
		ShopSwingView view = GuiActionRunner.execute(ShopSwingView::new);
		FrameFixture viewWindow = new FrameFixture(robot(), view);
		viewWindow.show();
		viewWindow.tabbedPane().selectTab(PRODUCTS_TAB_INDEX);
		viewWindow.tabbedPane().requireSelectedTab(atIndex(PRODUCTS_TAB_INDEX));
		viewWindow.tabbedPane().selectTab(CUSTOMERS_PURCHASES_TAB_INDEX);
		viewWindow.tabbedPane().requireSelectedTab(atIndex(CUSTOMERS_PURCHASES_TAB_INDEX));
		viewWindow.cleanUp();
	}
}
