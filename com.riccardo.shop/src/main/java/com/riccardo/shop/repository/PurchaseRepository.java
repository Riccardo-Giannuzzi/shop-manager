package com.riccardo.shop.repository;

import java.util.List;

import com.riccardo.shop.model.Purchase;

public interface PurchaseRepository {
	public List<Purchase> findAll() throws RepositoryException;

	public List<Purchase> findByCustomerId(String customerId) throws RepositoryException;

	public List<Purchase> findByProductId(String productId) throws RepositoryException;

	public Purchase findByCustomerIdAndProductId(String customerId, String productId) throws RepositoryException;

	public void save(Purchase purchase) throws RepositoryException;

	public void delete(String customerId, String productId) throws RepositoryException;
}
