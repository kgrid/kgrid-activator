A collection of HTTP requests has been created as the 'smoke test' script for testing the knowledge grid execution stack and associated shelf API upon deployment. The request collection is based on Postman collection schema.(Postman is the product of Postdot Technologies, Inc.)

To run the test script in Postman:

1. Import 'Execution_Stack_Smoke_Test.postman_collection.json';
2. Click on 'Manage Environments' in the environment setting dropdown and import 'SampleDev.postman_environment.json';
3. Select 'Sample Dev' as the environment;
4. If needed, edit the value for the key of 'url' to match the deployment path;
5. Run the collection.

To run the test script on command line using Newman:

1. If needed,  edit the value for the key of 'url' to match the deployment path in teh file of 'SampleDev.postman_environment.json';
2. Command line: newman run Execution_Stack_Smoke_Test.postman_collection.json -e SampleDev.postman_environment.json

To see the list of requests and sample inputs hit the "View Docs" button in postman next to the run button.
The tests are separated into two categories: ones that set up and expect to receive errors and ones that set up and expect success.


