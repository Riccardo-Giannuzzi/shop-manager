package com.riccardo.shop.app.swing;

import java.awt.EventQueue;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.Callable;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.riccardo.shop.controller.CustomerController;
import com.riccardo.shop.controller.ProductController;
import com.riccardo.shop.controller.PurchaseController;
import com.riccardo.shop.repository.CustomerRepository;
import com.riccardo.shop.repository.ProductRepository;
import com.riccardo.shop.repository.PurchaseRepository;
import com.riccardo.shop.repository.maria.CustomerMariaRepository;
import com.riccardo.shop.repository.maria.ProductMariaRepository;
import com.riccardo.shop.repository.maria.PurchaseMariaRepository;
import com.riccardo.shop.repository.mongo.CustomerMongoRepository;
import com.riccardo.shop.repository.mongo.ProductMongoRepository;
import com.riccardo.shop.repository.mongo.PurchaseMongoRepository;
import com.riccardo.shop.view.swing.ShopSwingView;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(mixinStandardHelpOptions = true)
public class ShopSwingApp implements Callable<Void> {

	@Option(names = { "--db-type" }, description = "Database type: mongo or maria")
	private String databaseType = "mongo";

	@Option(names = { "--mongo-host" }, description = "MongoDB host address")
	private String mongoHost = "localhost";

	@Option(names = { "--mongo-port" }, description = "MongoDB host port")
	private int mongoPort = 27017;

	@Option(names = {"--db-mongo-name"}, description = "MongoDB database name")
	private String databaseName = "shop";

	@Option(names = {"--db-mongo-product-collection"}, description = "Product collection name")
	private String productCollectionName = "product";

	@Option(names = {"--db-mongo-customer-collection"}, description = "Customer collection name")
	private String customerCollectionName = "customer";

	@Option(names = {"--db-mongo-purchase-collection"}, description = "Purchase collection name")
	private String purchaseCollectionName = "purchase";

	@Option(names = { "--maria-url" }, description = "MariaDB JDBC URL")
	private String mariaUrl = "jdbc:mariadb://localhost:3306/shop";

	@Option(names = { "--maria-user" }, description = "MariaDB username")
	private String mariaUser = "root";

	@Option(names = { "--maria-password" }, description = "MariaDB password")
	private String mariaPassword = "";

	public static void main(String[] args) {
		new CommandLine(new ShopSwingApp()).execute(args);
	}

	@Override
	public Void call() throws Exception {
		if (!"mongo".equalsIgnoreCase(databaseType) && !"maria".equalsIgnoreCase(databaseType)) {
			throw new IllegalArgumentException("Unknown database type: " + databaseType);
		}
		EventQueue.invokeLater(() -> {
			try {
				CustomerRepository customerRepository;
				ProductRepository productRepository;
				PurchaseRepository purchaseRepository;
				if ("mongo".equalsIgnoreCase(databaseType)) {
					MongoClient mongoClient = new MongoClient(new ServerAddress(mongoHost, mongoPort));
					customerRepository = new CustomerMongoRepository(mongoClient, databaseName, customerCollectionName);
					productRepository = new ProductMongoRepository(mongoClient, databaseName, productCollectionName);
					purchaseRepository = new PurchaseMongoRepository(mongoClient, databaseName, purchaseCollectionName);
				} else {
					Connection connection = DriverManager.getConnection(mariaUrl, mariaUser, mariaPassword);
					customerRepository = new CustomerMariaRepository(connection);
					productRepository = new ProductMariaRepository(connection);
					purchaseRepository = new PurchaseMariaRepository(connection);
				} 
				ShopSwingView shopSwingView = new ShopSwingView();
				CustomerController customerController = new CustomerController(shopSwingView.getCustomerPurchaseSwingView(), customerRepository, purchaseRepository);
				ProductController productController = new ProductController(shopSwingView.getProductSwingView(), productRepository, purchaseRepository);
				PurchaseController purchaseController = new PurchaseController(shopSwingView.getCustomerPurchaseSwingView(), purchaseRepository, customerRepository, productRepository);
				shopSwingView.setCustomerController(customerController);
				shopSwingView.setProductController(productController);
				shopSwingView.setPurchaseController(purchaseController);
				customerController.allCustomers();
				shopSwingView.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		return null;
	}
}