Feature: Shop View High Level
  Specifications of the behavior of the Shop View

  Background:
    Given The database contains a few customers
    And The database contains a few products
    And The Customer View is shown

  Scenario: Add a new customer
    Given The user provides customer data in the text fields
    When The user clicks the "Add" button
    Then The customer list contains the new customer

  Scenario: Delete a customer
    Given The user selects a customer from the list
    When The user clicks the "Delete Customer" button
    Then The customer is removed from the list

  Scenario: Add a purchase
    Given The user selects a customer from the list
    And The user selects a product from the available products
    When The user clicks the "Add Purchase" button
    Then The purchase list contains the new purchase