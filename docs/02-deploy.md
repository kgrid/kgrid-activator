---
layout: page
navtitle: Deploy the Activator
---
## Deploy

These instructions will get the Kgrid Activator running with sample set of Knowledge Objects.

### Prerequisites
For running the application you need:

- [JDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

### Running the Activator
Download the latest activator and a sample set of Knowledge Objects for testing the deployment. 
The latest release can be found on GitHub [Latest Activator Release](https://github.com/kgrid/kgrid-activator/releases/latest).

Download activator jar and sample_shelf.zip.  Unzip the sample_shelf.zip into the directory where the activator jar is located.

The activator is executable jar (running on port 8080 by default)
```java -jar kgrid-activator*.jar ```

Once running access the health endpoint. All _statuses_ reported should be **UP**
```curl http://localhost:8080/health```

## Validating the Activator 

With the sample shelf in place the following tests can be executed against the running activator

View a Knowledge Object

```curl http://localhost:8080/hello/world```

View a Knowledge Object Version

```curl http://localhost:8080/hello/world/v0.0.1```

Run the welcome endpoint on the hello/world/v0.0.1 knowledge object
```curl -X POST -H "Content-Type:application/json"  -d "{\"name\": \"Fred Flintstone\"}" http://localhost:8080/hello/world/v0.0.1/welcome```

## Configuration

**KO Shelf Location**
By default the activator will look for a _shelf_ in jar execution directory but the location the _shelf_ can be configured:
```bash
java -jar kgrid-activator*.jar --kgrid.shelf.cdostore.filesystem.location=//data/myshelf
```

**Activator port** 
To change the port:
```java -jar kgrid-activator*.jar --server.port=9090```

**Activator server path** 
To change the the server root path
```java -jar kgrid-activator*.jar --server.contextPath=/activator```