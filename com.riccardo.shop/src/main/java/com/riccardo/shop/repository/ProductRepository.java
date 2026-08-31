package com.riccardo.shop.repository;

import java.util.List;

import com.riccardo.shop.model.Product;

public interface ProductRepository {
	public List<Product> findAll() throws RepositoryException;

	public Product findById(String id) throws RepositoryException;

	public void save(Product product) throws RepositoryException;

	public void delete(String id) throws RepositoryException;
}
