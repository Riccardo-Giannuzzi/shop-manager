package com.riccardo.shop.view.swing;

import static org.assertj.swing.data.Index.atIndex;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(GUITestRunner.class)
public class ShopSwingViewTest extends AssertJSwingJUnitTestCase {

	private static final String PRODUCTS_TAB_TITLE = "Products";
	private static final String CUSTOMERS_PURCHASES_TAB_TITLE = "Customers Purchases";
	private static final int CUSTOMERS_PURCHASES_TAB_INDEX = 0;
	private static final int PRODUCTS_TAB_INDEX = 1;

	private FrameFixture window;

	private ShopSwingView shopSwingView;

	@Override
	protected void onSetUp() {
		GuiActionRunner.execute(
				() -> {
					shopSwingView = new ShopSwingView(new ProductSwingView(), new CustomerPurchaseSwingView());
					return shopSwingView;
				}
		);
		window = new FrameFixture(robot(), shopSwingView);
		window.show();
	}

	@Test @GUITest
	public void testControlsInitialStates() {
		window.requireTitle("Shop Manager");
		window.tabbedPane().requireTabTitles(CUSTOMERS_PURCHASES_TAB_TITLE,PRODUCTS_TAB_TITLE);
		window.tabbedPane().requireSelectedTab(atIndex(CUSTOMERS_PURCHASES_TAB_INDEX));
	}

	@Test
	public void testShouldSwitchBetweenTabs() {
		window.tabbedPane().selectTab(PRODUCTS_TAB_INDEX);
		window.tabbedPane().requireSelectedTab(atIndex(PRODUCTS_TAB_INDEX));
		window.tabbedPane().selectTab(CUSTOMERS_PURCHASES_TAB_INDEX);
		window.tabbedPane().requireSelectedTab(atIndex(CUSTOMERS_PURCHASES_TAB_INDEX));
	}
}
