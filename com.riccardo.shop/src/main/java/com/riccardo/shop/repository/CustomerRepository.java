package com.riccardo.shop.repository;

import java.util.List;

import com.riccardo.shop.model.Customer;

public interface CustomerRepository {
	public List<Customer> findAll() throws RepositoryException;

	public Customer findById(String id) throws RepositoryException;

	public void save(Customer customer) throws RepositoryException;

	public void delete(String id) throws RepositoryException;
}
