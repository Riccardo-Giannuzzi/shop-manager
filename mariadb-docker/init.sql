DROP TABLE IF EXISTS purchase;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS product;

CREATE TABLE customer (
  id VARCHAR(255) PRIMARY KEY,
  name VARCHAR(255) NOT NULL
);

CREATE TABLE product (
  id VARCHAR(255) PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  price DOUBLE NOT NULL
);

CREATE TABLE purchase (
  customer_id VARCHAR(255),
  product_id VARCHAR(255),
  PRIMARY KEY (customer_id, product_id),
  FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE RESTRICT,
  FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE RESTRICT
);