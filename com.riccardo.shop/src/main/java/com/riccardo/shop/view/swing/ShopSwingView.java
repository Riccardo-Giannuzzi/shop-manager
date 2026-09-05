package com.riccardo.shop.view.swing;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import com.riccardo.shop.controller.CustomerController;
import com.riccardo.shop.controller.ProductController;
import com.riccardo.shop.controller.PurchaseController;

public class ShopSwingView extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final int CUSTOMERS_PURCHASES_TAB_INDEX = 0;
	private static final int PRODUCTS_TAB_INDEX = 1;

	private transient ProductController productController;
	private transient CustomerController customerController;

	private ProductSwingView productSwingView;
	private CustomerPurchaseSwingView customerPurchaseSwingView;

	public ShopSwingView() {
		setTitle("Shop Manager");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		productSwingView = new ProductSwingView();
		customerPurchaseSwingView = new CustomerPurchaseSwingView();
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Customers Purchases", customerPurchaseSwingView);
		tabbedPane.addTab("Products", productSwingView);
		tabbedPane.addChangeListener(
				e -> {
					if (tabbedPane.getSelectedIndex() == CUSTOMERS_PURCHASES_TAB_INDEX && customerController != null) {
						customerController.allCustomers();
					} else if (tabbedPane.getSelectedIndex() == PRODUCTS_TAB_INDEX && productController != null) {
						productController.allProducts();
					}
				}
		);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		pack();
	}
	
	public ProductSwingView getProductSwingView() {
		return productSwingView;
	}

	public CustomerPurchaseSwingView getCustomerPurchaseSwingView() {
		return customerPurchaseSwingView;
	}

	public void setProductController(ProductController productController) {
		this.productController = productController;
		productSwingView.setProductController(productController);
	}

	public void setCustomerController(CustomerController customerController) {
		this.customerController = customerController;
		customerPurchaseSwingView.setCustomerController(customerController);
	}

	public void setPurchaseController(PurchaseController purchaseController) {
		customerPurchaseSwingView.setPurchaseController(purchaseController);
	}
}
