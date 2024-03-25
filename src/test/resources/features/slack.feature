@ignore
Feature: Slack Integration

  Using the @slack[] tag, you can send a notification to a slack channel(s) everytime your scenario fails.
  The message will contain the name of the scenario that failed as well as the feature file where it is
  located. You can fully configure the behavior of this tag using the configuration file located
  at src/test/resources/slack.properties
  More information here: https://github.com/josefd8/gingerspec/wiki/Gherkin-tags#slack-tag

  Rule: Getting slack notifications

    # This will send a notification message to qms-notifications channel if the scenario fails
    @slack[#qms-notifications]
    Scenario: Using the slack tag with one channel
      Then 'a' matches 'b'

      # This will send a notification message to qms-notifications and o1-e2e-test-report channel if the scenario fails
    @slack[#qms-notifications,#o1-e2e-test-report]
    Scenario: Using the slack tag with more than one channel
      Then 'a' matches 'b'

  Rule: Sending more complex messages in slack

    Scenario: Sending message to a slack channel
      Given I save 'GingerSpec' in variable 'FRAMEWORK'
      Given I send a message to the slack channel '#qms-notifications' with text
      """
      :wave: Hello! You can send any type of text to a given slack channel.
      You can even send variables created during your steps
      Regards, ${FRAMEWORK} :slightly_smiling_face:
      """