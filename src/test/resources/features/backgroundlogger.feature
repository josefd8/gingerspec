@rest

Feature: Simple logger test with background

  Background:
    Given I securely send requests to 'jsonplaceholder.typicode.com'

  Scenario: Some simple request
    When I send a 'GET' request to '/posts'
    Then the service response status must be '200'
    And the service response must contain the text 'body'

  Scenario: Some simple rest request
    When I send a 'GET' request to '/posts'
    Then the service response status must be '200'
    And I save element '$.[0].id' in environment variable 'VAR'
    When I send a 'GET' request to '/posts/!{VAR}'
    Then the service response status must be '200'
