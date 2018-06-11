---
layout: page
navtitle: Deploy the Activator
---
## Deploy Activator

These instructions will get the Kgrid Activator running with sample set of Knowledge Objects.

### Prerequisites

For running the application you need:

- [JDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

### Running the Activator

Download the latest activator and a sample set of Knowledge Objects for testing the deployment. 
The latest release can be found on GitHub [Latest Activator Release](https://github.com/kgrid/kgrid-activator/releases/latest).

1. Download _kgrid-activator-0.5.8-RC1.jar_ and _shelf.zip_.
1. Unzip the sample _shelf.zip_ into the directory where the activator jar is located.

Directory structure should look simular to the following

```     
 ├── shelf
 │   └── hello-world  
 │       └── v0.0.1
 │           ├── model
 │           └── metadata.json
 └── kgrid-activator-0.5.8-RC1.jar
```

The activator is executable jar and can be run from the command line.  Open a terminal window and navigate to the direcoty where the jar and shelf are located.  

Type in the following. 

```java -jar kgrid-activator-0.5.8-.jar ```

NOTE: By default the activator will run on port 8080.

## Validating the Activator 

Once running access the health endpoint. All _statuses_ reported should be **UP**

```curl http://localhost:8080/health```

With the sample shelf in place the following tests can be executed against the running activator

View a Knowledge Object

```curl http://localhost:8080/hello/world```

View a Knowledge Object Version

```curl http://localhost:8080/hello/world/v0.0.1```

Run the welcome endpoint on the hello/world/v0.0.1 knowledge object

```curl -X POST -H "Content-Type:application/json"  -d "{\"name\": \"Fred Flintstone\"}" http://localhost:8080/hello/world/v0.0.1/welcome```

## Configuration

**Activator Knowledge Object Shelf Location**

By default the activator will look for a _shelf_ in jar execution directory but the location the _shelf_ can be configured:

```bash
java -jar kgrid-activator-0.5.8-RC1.jar --kgrid.shelf.cdostore.filesystem.location=//data/myshelf
```

**Activator Server Port** 

To change the port:

```java -jar kgrid-activator-0.5.8-RC1.jar --server.port=9090```

Now access health

```curl http://localhost:9090/health```


**Activator Server Path** 

By default the endpoints of the activator at the root of the activator server.  To change the server root path:

```java -jar kgrid-activator-0.5.8-RC1.jar --server.contextPath=/activator```

Now access health

```curl http://localhost:8080/activator/health```