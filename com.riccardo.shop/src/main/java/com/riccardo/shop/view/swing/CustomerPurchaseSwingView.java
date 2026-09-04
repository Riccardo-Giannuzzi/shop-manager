package com.riccardo.shop.view.swing;

import java.util.List;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.riccardo.shop.controller.CustomerController;
import com.riccardo.shop.controller.PurchaseController;
import com.riccardo.shop.model.Customer;
import com.riccardo.shop.model.Product;
import com.riccardo.shop.model.Purchase;
import com.riccardo.shop.view.CustomerPurchaseView;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JList;

public class CustomerPurchaseSwingView extends JPanel implements CustomerPurchaseView {

	private static final long serialVersionUID = 1L;

	private JTextField txtId;
	private JTextField txtName;
	private JButton btnAdd;
	private JButton btnDeleteSelected;
	private JButton btnAddPurchase;
	private JButton btnDeletePurchase;
	private JLabel lblErrorMessage;

	private JList<Customer> listCustomers;
	private JList<Product> listAvailableProducts;
	private JList<Purchase> listPurchases;
	private DefaultListModel<Customer> listCustomersModel;
	private DefaultListModel<Purchase> listPurchasesModel;
	private DefaultListModel<Product> listAvailableProductsModel;

	private transient CustomerController customerController;
	private transient PurchaseController purchaseController;

	public CustomerPurchaseSwingView() {
		setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JPanel customerPanel = new JPanel();
		GridBagConstraints gbc_customerPanel = new GridBagConstraints();
		gbc_customerPanel.insets = new Insets(0, 0, 5, 5);
		gbc_customerPanel.fill = GridBagConstraints.BOTH;
		gbc_customerPanel.gridx = 0;
		gbc_customerPanel.gridy = 0;
		add(customerPanel, gbc_customerPanel);
		GridBagLayout gbl_customerPanel = new GridBagLayout();
		gbl_customerPanel.columnWidths = new int[]{0, 0, 0};
		gbl_customerPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_customerPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_customerPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		customerPanel.setLayout(gbl_customerPanel);
		
		JLabel lblCustomers = new JLabel("Customers");
		GridBagConstraints gbc_lblCustomers = new GridBagConstraints();
		gbc_lblCustomers.gridwidth = 2;
		gbc_lblCustomers.insets = new Insets(0, 0, 5, 0);
		gbc_lblCustomers.gridx = 0;
		gbc_lblCustomers.gridy = 0;
		customerPanel.add(lblCustomers, gbc_lblCustomers);
		
		JLabel lblId = new JLabel("id");
		GridBagConstraints gbc_lblId = new GridBagConstraints();
		gbc_lblId.anchor = GridBagConstraints.EAST;
		gbc_lblId.insets = new Insets(0, 0, 5, 5);
		gbc_lblId.gridx = 0;
		gbc_lblId.gridy = 1;
		customerPanel.add(lblId, gbc_lblId);
		
		txtId = new JTextField();
		KeyAdapter btnAddEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnAdd.setEnabled(
						!txtId.getText().trim().isEmpty()
						&& !txtName.getText().trim().isEmpty()
				);
			}
		};
		txtId.addKeyListener(btnAddEnabler);
		txtId.setName("idTextBox");
		GridBagConstraints gbc_txtId = new GridBagConstraints();
		gbc_txtId.insets = new Insets(0, 0, 5, 0);
		gbc_txtId.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtId.gridx = 1;
		gbc_txtId.gridy = 1;
		customerPanel.add(txtId, gbc_txtId);
		txtId.setColumns(10);
		
		JLabel lblName = new JLabel("name");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 2;
		customerPanel.add(lblName, gbc_lblName);
		
		txtName = new JTextField();
		txtName.addKeyListener(btnAddEnabler);
		txtName.setName("nameTextBox");
		GridBagConstraints gbc_txtName = new GridBagConstraints();
		gbc_txtName.insets = new Insets(0, 0, 5, 0);
		gbc_txtName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtName.gridx = 1;
		gbc_txtName.gridy = 2;
		customerPanel.add(txtName, gbc_txtName);
		txtName.setColumns(10);
		
		btnAdd = new JButton("Add");
		btnAdd.setEnabled(false);
		btnAdd.addActionListener(
				e -> customerController.newCustomer(
						new Customer(
								txtId.getText(),
								txtName.getText()
						)
				)
		);
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.insets = new Insets(0, 0, 5, 0);
		gbc_btnAdd.gridwidth = 2;
		gbc_btnAdd.gridx = 0;
		gbc_btnAdd.gridy = 3;
		customerPanel.add(btnAdd, gbc_btnAdd);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 2;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 4;
		customerPanel.add(scrollPane, gbc_scrollPane);
		
		listCustomersModel = new DefaultListModel<>();
		listCustomers = new JList<>(listCustomersModel);
		scrollPane.setViewportView(listCustomers);
		listCustomers.addListSelectionListener(
				e -> {
					if (!e.getValueIsAdjusting()) {
						boolean customerSelected = listCustomers.getSelectedIndex() != -1;
						btnDeleteSelected.setEnabled(customerSelected);
						listAvailableProducts.setEnabled(customerSelected);
						listPurchases.setEnabled(customerSelected);
						if (customerSelected) {
							Customer customer = listCustomers.getSelectedValue();
							purchaseController.allCustomerPurchases(customer);
							purchaseController.allCustomerAvailableProducts(customer);
						} else {
							btnAddPurchase.setEnabled(false);
							btnDeletePurchase.setEnabled(false);
						}
					}
				}
		);
		listCustomers.setName("customerList");
		listCustomers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		btnDeleteSelected = new JButton("Delete Customer");
		btnDeleteSelected.setEnabled(false);
		btnDeleteSelected.addActionListener(
				e -> customerController.deleteCustomer(
						listCustomers.getSelectedValue()
				)
		);
		GridBagConstraints gbc_btnDeleteSelected = new GridBagConstraints();
		gbc_btnDeleteSelected.gridwidth = 2;
		gbc_btnDeleteSelected.insets = new Insets(0, 0, 0, 5);
		gbc_btnDeleteSelected.gridx = 0;
		gbc_btnDeleteSelected.gridy = 5;
		customerPanel.add(btnDeleteSelected, gbc_btnDeleteSelected);
		
		JPanel purchasePanel = new JPanel();
		GridBagConstraints gbc_purchasePanel = new GridBagConstraints();
		gbc_purchasePanel.insets = new Insets(0, 0, 5, 0);
		gbc_purchasePanel.fill = GridBagConstraints.BOTH;
		gbc_purchasePanel.gridx = 1;
		gbc_purchasePanel.gridy = 0;
		add(purchasePanel, gbc_purchasePanel);
		GridBagLayout gbl_purchasePanel = new GridBagLayout();
		gbl_purchasePanel.columnWidths = new int[]{0, 0};
		gbl_purchasePanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_purchasePanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_purchasePanel.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		purchasePanel.setLayout(gbl_purchasePanel);
		
		JLabel lblAvailableProducts = new JLabel("Available Products");
		GridBagConstraints gbc_lblAvailableProducts = new GridBagConstraints();
		gbc_lblAvailableProducts.insets = new Insets(0, 0, 5, 0);
		gbc_lblAvailableProducts.gridx = 0;
		gbc_lblAvailableProducts.gridy = 0;
		purchasePanel.add(lblAvailableProducts, gbc_lblAvailableProducts);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 1;
		purchasePanel.add(scrollPane_1, gbc_scrollPane_1);
		
		listAvailableProductsModel = new DefaultListModel<>();
		listAvailableProducts = new JList<>(listAvailableProductsModel);
		listAvailableProducts.setEnabled(false);
		scrollPane_1.setViewportView(listAvailableProducts);
		listAvailableProducts.setName("availableProductList");
		listAvailableProducts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		btnAddPurchase = new JButton("Add Purchase");
		btnAddPurchase.setEnabled(false);
		listAvailableProducts.addListSelectionListener(
				e -> {
					if (!e.getValueIsAdjusting()) {
						btnAddPurchase.setEnabled(listAvailableProducts.getSelectedIndex() != -1);
					}
				}
		);
		btnAddPurchase.addActionListener(
				e -> purchaseController.newPurchase(
						new Purchase(
								listCustomers.getSelectedValue().getId(),
								listAvailableProducts.getSelectedValue().getId()
						)
				)
		);
		GridBagConstraints gbc_btnAddPurchase = new GridBagConstraints();
		gbc_btnAddPurchase.insets = new Insets(0, 0, 5, 0);
		gbc_btnAddPurchase.gridx = 0;
		gbc_btnAddPurchase.gridy = 2;
		purchasePanel.add(btnAddPurchase, gbc_btnAddPurchase);
		
		JLabel lblPurchases = new JLabel("Purchases");
		GridBagConstraints gbc_lblPurchases = new GridBagConstraints();
		gbc_lblPurchases.insets = new Insets(0, 0, 5, 0);
		gbc_lblPurchases.gridx = 0;
		gbc_lblPurchases.gridy = 3;
		purchasePanel.add(lblPurchases, gbc_lblPurchases);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
		gbc_scrollPane_2.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_2.gridx = 0;
		gbc_scrollPane_2.gridy = 4;
		purchasePanel.add(scrollPane_2, gbc_scrollPane_2);
		
		listPurchasesModel = new DefaultListModel<>();
		listPurchases = new JList<>(listPurchasesModel);
		listPurchases.setEnabled(false);
		scrollPane_2.setViewportView(listPurchases);
		listPurchases.setName("purchaseList");
		listPurchases.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		btnDeletePurchase = new JButton("Delete Purchase");
		btnDeletePurchase.setEnabled(false);
		listPurchases.addListSelectionListener(
				e -> {
					if (!e.getValueIsAdjusting()) {
						btnDeletePurchase.setEnabled(listPurchases.getSelectedIndex() != -1);
					}
				}
		);
		btnDeletePurchase.addActionListener(
				e -> purchaseController.deletePurchase(
						listPurchases.getSelectedValue()
				)
		);
		GridBagConstraints gbc_btnDeletePurchase = new GridBagConstraints();
		gbc_btnDeletePurchase.gridx = 0;
		gbc_btnDeletePurchase.gridy = 5;
		purchasePanel.add(btnDeletePurchase, gbc_btnDeletePurchase);
		
		lblErrorMessage = new JLabel(" ");
		lblErrorMessage.setName("errorMessageLabel");
		GridBagConstraints gbc_lblErrorMessage = new GridBagConstraints();
		gbc_lblErrorMessage.gridwidth = 2;
		gbc_lblErrorMessage.gridx = 0;
		gbc_lblErrorMessage.gridy = 1;
		add(lblErrorMessage, gbc_lblErrorMessage);

	}

	DefaultListModel<Customer> getListCustomersModel() {
		return listCustomersModel;
	}

	public void setCustomerController(CustomerController customerController) {
		this.customerController = customerController;
	}

	public void setPurchaseController(PurchaseController purchaseController) {
		this.purchaseController = purchaseController;
	}

	@Override
	public void showAllCustomers(List<Customer> customers) {
		customers.forEach(listCustomersModel::addElement);
	}

	@Override
	public void showAllCustomerPurchases(List<Purchase> purchases) {
		listPurchasesModel.clear();
		purchases.forEach(listPurchasesModel::addElement);
	}

	@Override
	public void showAllCustomerAvailableProducts(List<Product> products) {
		listAvailableProductsModel.clear();
		products.forEach(listAvailableProductsModel::addElement);
	}

	@Override
	public void showError(String message) {
		lblErrorMessage.setText(message);
	}

	@Override
	public void showError(String message, Customer customer) {
		lblErrorMessage.setText(message + ": " + customer);
	}

	@Override
	public void showError(String message, Purchase purchase) {
		lblErrorMessage.setText(message + ": " + purchase);
	}

	@Override
	public void customerAdded(Customer customer) {
		listCustomersModel.addElement(customer);
		resetErrorLabel();
	}

	@Override
	public void customerRemoved(Customer customer) {
		listCustomersModel.removeElement(customer);
		resetErrorLabel();
	}

	@Override
	public void purchaseAdded(Purchase purchase) {
		refreshSelectedCustomerData();
		resetErrorLabel();
	}

	@Override
	public void purchaseRemoved(Purchase purchase) {
		refreshSelectedCustomerData();
		resetErrorLabel();
	}

	private void resetErrorLabel() {
		lblErrorMessage.setText(" ");
	}

	private void refreshSelectedCustomerData() {
		Customer customer = listCustomers.getSelectedValue();
		if (customer != null) {
			purchaseController.allCustomerPurchases(customer);
			purchaseController.allCustomerAvailableProducts(customer);
		}
	}
}
