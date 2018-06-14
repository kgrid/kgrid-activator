---
layout: page
navtitle: Build the Activator
---
## Build Activator

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites
For building and running the application you need:

- [JDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Maven 3](https://maven.apache.org)

### Clone
To get started you can simply clone this repository using git:
```
git clone https://github.com/kgrid/kgrid-activator.git
cd kgrid-activator
```

### Quick Start
This quick start will run the activator and load two example knowledge objects for testing.  This objects are located
in the _shelf_ directory at the root of the project. By default application will start up and PORT 8080.
```
mvn clean package
java -jar target/kgrid-activator*.jar
```
You can load sample KO shelf (_where to look for the KOs_)
```
java -jar target/kgrid-activator*.jar --kgrid.shelf.cdostore.url:filesystem:file://etc/shelf
```
Alternatively you can use the [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html) like so:

```
mvn clean spring-boot:run
```

Once Running access the [Activators Health Endpoint](http://localhost:8080/health).  All _statuses_ reported should be **UP**

```$xslt
"status": "UP",
    "shelf": {
        "status": "UP",
    
    ...
    
    "activationService": {
            "status": "UP",
    
    ...
    
   "diskSpace": {
           "status": "UP",     
```

## Running the tests

#### Automated tests 
Unit and Integration tests can be executed via
```
mvn clean test
mvn clean verify
```

#### End to End Testing

Sample shelf in place the following tests can be executed against the running activator

View a Knowledge Object

```
curl http://localhost:8080/hello/world
```

View a Knowledge Object Version

```
curl http://localhost:8080/hello/world/v0.0.1
```

Run the welcome endpoint on the 99999/newko/v0.0.1 knowledge object
```
curl -X POST -H "Content-Type:application/json"  -d "{\"name\": \"Fred Flintstone\"}" http://localhost:8080/hello/world/v0.0.1/welcome
```

