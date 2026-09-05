Feature: Shop View
  Specifications of the behavior of the Shop View

  Scenario: The initial state of the view
    Given The database contains the customers with the following values
      | C1 | customer1 |
      | C2 | customer2 |
    When The Customer View is shown
    Then The customer list contains elements with the following values
      | C1 | customer1 |
      | C2 | customer2 |

  Scenario: Add a new customer
    Given The Customer View is shown
    When The user enters the following values in the text fields
      | id | name        |
      | C1 | newCustomer |
    And The user clicks the "Add" button
    Then The customer list contains elements with the following values
      | C1 | newCustomer |

  Scenario: Delete an existing customer
    Given The database contains the customers with the following values
      | C1 | customer1 |
    And The Customer View is shown
    When The user selects the customer with id "C1"
    And The user clicks the "Delete Customer" button
    Then The customer list is empty

  Scenario: Add a new customer with an existing id
    Given The database contains the customers with the following values
      | C1 | customer1 |
    And The Customer View is shown
    When The user enters the following values in the text fields
      | id | name        |
      | C1 | newCustomer |
    And The user clicks the "Add" button
    Then An error is shown containing the following values
      | C1 | customer1 |

  Scenario: Add a purchase for an existing customer
    Given The database contains the customers with the following values
      | C1 | customer1 |
    And The database contains the products with the following values
      | P1 | product1  | 10.0 |
    And The Customer View is shown
    When The user selects the customer with id "C1"
    And The user selects the product with id "P1" from the available products
    And The user clicks the "Add Purchase" button
    Then The purchase list contains a purchase with customer id "C1" and product id "P1"

  Scenario: Delete an existing purchase
    Given The database contains the customers with the following values
      | C1 | customer1 |
    And The database contains the products with the following values
      | P1 | product1  | 10.0 |
    And The database contains the purchases with the following values
      | C1 | P1        |
    And The Customer View is shown
    When The user selects the customer with id "C1"
    And The user selects the purchase with customer id "C1" and product id "P1"
    And The user clicks the "Delete Purchase" button
    Then The purchase list is empty

  Scenario: Add a new product
    Given The Customer View is shown
    When The user selects the "Products" tab
    And The user enters the following values in the text fields
      | id | name     | price |
      | P3 | product3 | 30.0  |
    And The user clicks the "Add" button
    Then The product list contains elements with the following values
      | P3 | product3 | 30.0 |

  Scenario: Delete an existing product
    Given The database contains the products with the following values
      | P1 | product1 | 10.0 |
    And The Customer View is shown
    When The user selects the "Products" tab
    And The user selects the product with id "P1"
    And The user clicks the "Delete Selected" button
    Then The product list is empty

  Scenario: Add a new product with an existing id
    Given The database contains the products with the following values
      | P1 | product1 | 10.0 |
    And The Customer View is shown
    When The user selects the "Products" tab
    And The user enters the following values in the text fields
      | id | name       | price |
      | P1 | newProduct | 20.0  |
    And The user clicks the "Add" button
    Then An error is shown containing the following values
      | P1 | product1 |

  Scenario: Delete a customer that has a purchase
    Given The database contains the customers with the following values
      | C1 | customer1 |
    And The database contains the products with the following values
      | P1 | product1 | 10.0 |
    And The database contains the purchases with the following values
      | C1 | P1 |
    And The Customer View is shown
    When The user selects the customer with id "C1"
    And The user clicks the "Delete Customer" button
    Then An error is shown containing the following values
      | C1 | customer1 |

  Scenario: Delete a product that has a purchase
    Given The database contains the customers with the following values
      | C1 | customer1 |
    And The database contains the products with the following values
      | P1 | product1 | 10.0 |
    And The database contains the purchases with the following values
      | C1 | P1 |
    And The Customer View is shown
    When The user selects the "Products" tab
    And The user selects the product with id "P1"
    And The user clicks the "Delete Selected" button
    Then An error is shown containing the following values
      | P1 | product1 |
