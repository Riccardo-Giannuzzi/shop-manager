package com.riccardo.shop.view.swing;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.riccardo.shop.controller.ProductController;
import com.riccardo.shop.model.Product;
import com.riccardo.shop.view.ProductView;

import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JList;

public class ProductSwingView extends JPanel implements ProductView {

	private static final long serialVersionUID = 1L;

	private JTextField txtId;
	private JTextField txtName;
	private JTextField txtPrice;
	private JButton btnAdd;
	private JButton btnDeleteSelected;
	private JLabel lblErrorMessage;

	private JList<Product> listProducts;
	private DefaultListModel<Product> listProductsModel;

	private transient ProductController productController;

	public ProductSwingView() {
		setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{0, 0, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gbl_contentPane);
		
		JLabel lblId = new JLabel("id");
		GridBagConstraints gbc_lblId = new GridBagConstraints();
		gbc_lblId.insets = new Insets(0, 0, 5, 5);
		gbc_lblId.anchor = GridBagConstraints.EAST;
		gbc_lblId.gridx = 0;
		gbc_lblId.gridy = 0;
		add(lblId, gbc_lblId);
		
		txtId = new JTextField();
		KeyAdapter btnAddEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnAdd.setEnabled(
						!txtId.getText().trim().isEmpty()
						&& !txtName.getText().trim().isEmpty()
						&& isPriceValid()
				);
			}
		};
		txtId.addKeyListener(btnAddEnabler);
		txtId.setName("idTextBox");
		GridBagConstraints gbc_txtId = new GridBagConstraints();
		gbc_txtId.insets = new Insets(0, 0, 5, 5);
		gbc_txtId.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtId.gridx = 1;
		gbc_txtId.gridy = 0;
		add(txtId, gbc_txtId);
		txtId.setColumns(10);
		
		JLabel lblName = new JLabel("name");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 1;
		add(lblName, gbc_lblName);
		
		txtName = new JTextField();
		txtName.addKeyListener(btnAddEnabler);
		txtName.setName("nameTextBox");
		GridBagConstraints gbc_txtName = new GridBagConstraints();
		gbc_txtName.insets = new Insets(0, 0, 5, 5);
		gbc_txtName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtName.gridx = 1;
		gbc_txtName.gridy = 1;
		add(txtName, gbc_txtName);
		txtName.setColumns(10);
		
		JLabel lblPrice = new JLabel("price");
		GridBagConstraints gbc_lblPrice = new GridBagConstraints();
		gbc_lblPrice.anchor = GridBagConstraints.EAST;
		gbc_lblPrice.insets = new Insets(0, 0, 5, 5);
		gbc_lblPrice.gridx = 0;
		gbc_lblPrice.gridy = 2;
		add(lblPrice, gbc_lblPrice);
		
		txtPrice = new JTextField();
		txtPrice.addKeyListener(btnAddEnabler);
		txtPrice.setName("priceTextBox");
		GridBagConstraints gbc_txtPrice = new GridBagConstraints();
		gbc_txtPrice.insets = new Insets(0, 0, 5, 5);
		gbc_txtPrice.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtPrice.gridx = 1;
		gbc_txtPrice.gridy = 2;
		add(txtPrice, gbc_txtPrice);
		txtPrice.setColumns(10);
		
		btnAdd = new JButton("Add");
		btnAdd.setEnabled(false);
		btnAdd.addActionListener(
				e -> productController.newProduct(
						new Product(
								txtId.getText(),
								txtName.getText(),
								Double.parseDouble(txtPrice.getText())
						)
				)
		);
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.gridwidth = 2;
		gbc_btnAdd.insets = new Insets(0, 0, 5, 5);
		gbc_btnAdd.gridx = 0;
		gbc_btnAdd.gridy = 3;
		add(btnAdd, gbc_btnAdd);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 2;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 4;
		add(scrollPane, gbc_scrollPane);
		
		listProductsModel = new DefaultListModel<>();
		listProducts = new JList<>(listProductsModel);
		listProducts.addListSelectionListener(
				e -> btnDeleteSelected.setEnabled(listProducts.getSelectedIndex() != -1));
		listProducts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listProducts.setName("productList");
		
		scrollPane.setViewportView(listProducts);
		
		btnDeleteSelected = new JButton("Delete Selected");
		btnDeleteSelected.setEnabled(false);
		btnDeleteSelected.addActionListener(
				e -> productController.deleteProduct(listProducts.getSelectedValue())
		);
		GridBagConstraints gbc_btnDeleteSelected = new GridBagConstraints();
		gbc_btnDeleteSelected.insets = new Insets(0, 0, 5, 5);
		gbc_btnDeleteSelected.gridwidth = 2;
		gbc_btnDeleteSelected.gridx = 0;
		gbc_btnDeleteSelected.gridy = 5;
		add(btnDeleteSelected, gbc_btnDeleteSelected);
		
		lblErrorMessage = new JLabel(" ");
		lblErrorMessage.setName("errorMessageLabel");
		GridBagConstraints gbc_lblErrorMessage = new GridBagConstraints();
		gbc_lblErrorMessage.gridwidth = 2;
		gbc_lblErrorMessage.insets = new Insets(0, 0, 0, 5);
		gbc_lblErrorMessage.gridx = 0;
		gbc_lblErrorMessage.gridy = 6;
		add(lblErrorMessage, gbc_lblErrorMessage);

	}

	DefaultListModel<Product> getListProductsModel() {
		return listProductsModel;
	}

	public void setProductController(ProductController productController) {
		this.productController = productController;
	}

	@Override
	public void showAllProducts(List<Product> products) {
		listProductsModel.clear();
		products.forEach(listProductsModel::addElement);
	}

	@Override
	public void showError(String message) {
		lblErrorMessage.setText(message);
	}

	@Override
	public void showError(String message, Product product) {
		lblErrorMessage.setText(message + ": " + product);
	}

	@Override
	public void productAdded(Product product) {
		listProductsModel.addElement(product);
		resetErrorLabel();
	}

	@Override
	public void productRemoved(Product product) {
		listProductsModel.removeElement(product);
		resetErrorLabel();
	}

	private void resetErrorLabel() {
		lblErrorMessage.setText(" ");
	}

	private boolean isPriceValid() {
		try {
			Double.parseDouble(txtPrice.getText().trim());
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
