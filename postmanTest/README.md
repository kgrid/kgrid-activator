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


Currently, the collection will submit the following requests and examine the resposes:

1. GET the list of KOs on the execution stack shelf. A status code of 200 is expected to pass the test.
2. PUT a sample Test Knowledge object to the shelf. A success message is expected to indicator the object has been added.
3. GET the detail of the knowledge object from the shelf. Payload, Input message and output message are expected to be present.
4. POST test data to execute the knowledge object. A proper result is expected to test the test.


