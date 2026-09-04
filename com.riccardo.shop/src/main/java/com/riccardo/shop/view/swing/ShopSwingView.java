package com.riccardo.shop.view.swing;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

public class ShopSwingView extends JFrame {

	private static final long serialVersionUID = 1L;

	public ShopSwingView(ProductSwingView productSwingView, CustomerPurchaseSwingView customerPurchaseSwingView) {
		setTitle("Shop Manager");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Customers Purchases", customerPurchaseSwingView);
		tabbedPane.addTab("Products", productSwingView);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		pack();
	}
}
