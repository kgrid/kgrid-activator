# Knowledge Grid — Python Execution Stack

## Installing

    git clone https://github.com/kgrid/python-execution-stack.git

    cd python-execution-stack

Build and deploy the execution stack (the executable `.war` file can be run as a standard jar file):

    mvn clean package

    java -Dpython.import.site=false -jar target/python-execution-stack-0.0.1-SNAPSHOT.war

Or the same `.war` file can be deployed in a Tomcat 8+ container. (The `-Dpython.import.site=false` is required when running as a
bare jar file.  It is not required when the war file is deployed in a container.)


The following properties can be configured as system defaults on the command line using '`-Doption=value`:


    -library.url=http://dlhs-fedora-dev-a.umms.med.umich.edu:8080/ObjectTeller    # Default is no external library

    -Dstack.shelf.path=/Users/kgrid/ko/    # Default is system property 'java.io.tmpdir'

    -Dstack.shelf.name=mycoolshelf    # Default is 'shelf' in the local storage path identified above

    -Dserver.port=8082    # Default is '8080'

In all cases properties can be specified via a system property, or by passing
as parameters on startup, or any of the typical Tomcat property config methods.

    export SERVER_PORT=8083

It is also possible to supply the location of an external application properties file:

    -Dspring.config.location=file:/path/to/config/folder'

 by default it will load configuration from application.properties
    or you can supply different application properties file using

    spring.config.location=file:/path/to/config/folder/my-application.properties

> See [Spring Boot external property configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html) for more detail.


## Loading an object and testing the execution stack

The `shelf` API allows knowledge objects to be loaded from an attached library or uploaded manually.

#### Manual loading

A knowledge object supplied in the body of a `PUT` request can be uploaded directly to
the shelf with a user  supplied ark id:

```
curl -X PUT -H "Content-Type: application/json" -d '{
"metadata": {
    "title": "Hello World",
    "owner": "",
    "description": "Test object, echos name",
    "contributors": "",
    "keywords": "echo",
    "published": true
},
"inputMessage": "<rdf:RDF xmlns:ot=\"http://uofm.org/objectteller/#\"\n         xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n    <rdf:Description rdf:about=\"http://uofm.org/objectteller/inputMessage\">\n        <ot:noofparams>1</ot:noofparams>\n        <ot:params>\n            <rdf:Seq>\n                <rdf:li>name</rdf:li>\n            </rdf:Seq>\n        </ot:params>\n    </rdf:Description>\n    <rdf:Description rdf:about=\"http://uofm.org/objectteller/inputs/\">\n        <ot:datatype>STRING</ot:datatype>\n    </rdf:Description>\n</rdf:RDF>\n",
"outputMessage": "<rdf:RDF xmlns:ot=\"http://uofm.org/objectteller/\"\n  xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n  <rdf:Description rdf:about=\"http://uofm.org/objectteller/outputMessage\">\n    <ot:returntype>STRING</ot:returntype>\n  </rdf:Description>\n</rdf:RDF>\n",
"payload": {
    "content": "def execute(inputs):\n    name = inputs[\"name\"]\n    return \"Hello, \" + name\n\n#print echo({\"inputs\":\"hello world\"})\n",
    "functionName": "execute",
    "engineType": "Python"
}
}' "http://localhost:8080/knowledgeObject/ark:/hello/world"
```

this object is then visible at:

```bash
curl -X GET "http://localhost:8080/knowledgeObject/ark:/hello/world"
  ```

and can be invoked with:

```bash
curl -X POST -H "Content-Type: application/json" -d '{ "name": "Peter" }' "http://localhost:8080/knowledgeObject/ark:/hello/world/result"
```

#### Loading from an attached library


```bash
curl -X PUT "http://localhost:8080/knowledgeObject/ark:/99999/fk45h7sd3p"
   ```

and the same `GET` and `POST` operations are available.

#### Test 'shelf' objects

Example knowledge objects are included in the source repository in 'etc/shelf' directory.
You can load these  objects manually as outlined above, or start the execution stack
pointing to this shelf.

```bash
java -Dpython.import.site=false -Dstack.shelf.path=etc -jar target/python-execution-stack-0.0.1-SNAPSHOT.war
```


