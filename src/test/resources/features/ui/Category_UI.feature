@UI @Category
Feature: Category Management UI
  As an Admin, I want to manage categories
  So that I can organize the inventory

  Background:
    Given I open the application
    # This automatically uses the 'admin' credentials defined in Java
    And I am logged in as "Admin" 
    And I navigate to the "Categories" page

  @UI-CAT-Create-01
  Scenario: Verify Admin can create a new category successfully
    When I click the "Add Category" button
    And I enter "TestCat" in the category name field
    And I click the "Save" button
    Then I should see a success message "Category created successfully"
    And I should see "TestCat" in the category list

  @UI-CAT-CreateValidate-02
  Scenario Outline: Verify Category Name Validation
    When I click the "Add Category" button
    And I enter "<Name>" in the category name field
    And I click the "Save" button
    Then I should see a validation error "<ErrorMessage>"

    Examples:
      | Name             | ErrorMessage                                       |
      | AB               | Category name must be between 3 and 10 characters. |
      | ThisNameIsTooLong| Category name must be between 3 and 10 characters. |
      |                  | Category name is required.                         |

  @UI-CAT-Edit-03
  Scenario: Verify Admin can Edit an existing category
    When I click the "Edit" button for the category "TestCat"
    And I enter "CatUpdated" in the category name field
    And I click the "Save" button
    Then I should see a success message "Category updated successfully"
    And I should see "CatUpdated" in the category list

   @UI-CAT-EditValidate-04
   Scenario Outline: Verify Category Edit Validation
    When I click the "Edit" button for the category "CatUpdated"
    And I enter "<Name>" in the category name field
    And I click the "Save" button
    Then I should see a validation error "<ErrorMessage>"

    Examples:
      | Name             | ErrorMessage                                       |
      | AB               | Category name must be between 3 and 10 characters. |
      | ThisNameIsTooLong| Category name must be between 3 and 10 characters. |
      |                  | Category name is required.                         |

  @UI-CAT-Cancel-05
  Scenario: Verify Admin can Cancel editing
    When I click the "Edit" button for the category "CatUpdated"
    And I click the "Cancel" button
    # Verifies we are back on the list page by checking for the "Add" button existence
    Then I should see the "Add Category" button

  @UI-CAT-Delete-06
  Scenario: Verify Admin can Delete a category
    When I click the "Delete" button for the category "CatUpdated"
    And I accept the delete confirmation
    Then I should see a success message "Category deleted successfully"

  

  @Regression
  @UI-CAT-Search-07
  Scenario Outline: Verify Search functionality
    When I enter "<SearchTerm>" in the search box
    And I click the search button
    Then I should see "<ExpectedResult>" in the category list

    Examples:
      | SearchTerm | ExpectedResult    |
      | Flowers    | Flowers           |
      | InvalidCat | No category found |

  @UI-CAT-RBAC-08
  Scenario: Verify User (Non-Admin) cannot see management buttons
    # Since Background logs in as Admin, we must logout first
    Given I click the "Logout" button
    # This automatically uses the 'testuser' credentials defined in Java
    And I am logged in as "User"
    When I navigate to the "Categories" page
    Then I should not see the "Add Category" button
    And I should not see the "Delete" buttons

  # ========== Additional Search Tests ==========

  @Regression
  @UI-CAT-SearchParent-09
  Scenario: Verify search by parent category only
    When I select "Flowers" from the parent category dropdown
    And I click the search button
    Then I should see categories with parent "Flowers"

  @Regression
  @UI-CAT-SearchBoth-10
  Scenario: Verify search by both parent and sub-category
    When I select "Flowers" from the parent category dropdown
    And I enter "Roses" in the search box
    And I click the search button
    Then I should see "Roses" in the category list
    And the displayed categories should have parent "Flowers"

  # ========== Additional Creation Tests ==========

  @UI-CAT-CreateParent-11
  Scenario: Verify Admin can create category with parent selected
    When I click the "Add Category" button
    And I enter "ChildCat" in the category name field
    And I select "Trees" from the parent dropdown
    And I click the "Save" button
    Then I should see a success message "Category created successfully"
    And I should see "ChildCat" in the category list

  @UI-CAT-CreateList-12
  Scenario: Verify created category appears in list correctly
    When I click the "Add Category" button
    And I enter "NewItem" in the category name field
    And I click the "Save" button
    Then I should see a success message "Category created successfully"
    And I should see "NewItem" in the category list
    And the category "NewItem" should be displayed with correct details

  # ========== Additional Edit Tests ==========

  @UI-CAT-EditList-13
  Scenario: Verify updated category displays correctly in list
    When I click the "Edit" button for the category "NewItem"
    And I enter "UpdatedNew" in the category name field
    And I click the "Save" button
    Then I should see a success message "Category updated successfully"
    And I should see "UpdatedNew" in the category list
    And I should not see "NewItem" in the category list

  # ========== Additional RBAC Tests ==========

  @UI-CAT-AdminView-14
  Scenario: Verify Admin can see all management buttons
    Then I should see the "Add Category" button
    And I should see the "Edit" buttons for categories
    And I should see the "Delete" buttons for categories

  @UI-CAT-UserView-15
  Scenario: Verify User cannot see Edit buttons
    Given I click the "Logout" button
    And I am logged in as "User"
    When I navigate to the "Categories" page
    Then I should not see the "Edit" buttons

 