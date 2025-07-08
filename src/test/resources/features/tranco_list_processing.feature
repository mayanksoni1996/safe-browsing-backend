Feature: Tranco List Processing
  As a user of the Safe Browsing service
  I want the system to download and process Tranco lists
  So that I can have up-to-date trusted domain data

  Background:
    Given the MinIO service is available
    And the MongoDB service is available

  Scenario: Download and process a Tranco list
    Given a Tranco list is available from the Tranco API
    When the system downloads the Tranco list
    Then the list should be stored in MinIO
    And the list metadata should be stored in MongoDB
    And the domains from the list should be processed and stored in MongoDB

  Scenario: Process an existing Tranco list from MinIO
    Given a Tranco list file exists in MinIO
    And the list metadata exists in MongoDB with processed status as false
    When the system processes the Tranco list
    Then the domains from the list should be processed and stored in MongoDB
    And the list metadata should be updated with processed status as true