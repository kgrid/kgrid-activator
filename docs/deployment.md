## Deploy

These instructions will get the Kgrid Activator running with sample set of Knowledge Objects.

### Prerequisites
For running the application you need:

- [JDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

### Running the Activator
Download the latest activator and a sample set of Knowledge Objects for testing the deployment. 
The latest release can be found on GitHub [Latest Activator Release](https://github.com/kgrid/kgrid-activator/releases/latest).

Download activator jar and sample_shelf.zip.  Unzip the sample_shelf.zip into the directory where the activator jar is located.
f
The activator is executable jar (running on port 8080 by default)e to look for the KOs_)
```
java -jar kgrid-activator*.jar 
```

Once Running access the health endpoint. All _statuses_ reported should be **UP**
```
curl http://localhost:8080/health
```

Example response from the activator health endpoint.  
```$xslt
{
  "status" : "UP",
  "shelf" : {
    "status" : "UP",
    "kgrid.shelf.cdostore.*.location" : "file:///Users/developer/Downloads/activator/shelf/"
  },
  "activationService" : {
    "status" : "UP",
    "Knowledge Objects found" : 3,
    "Adapters loaded" : [ "JAVASCRIPT" ],
    "Executors loaded" : [ "hello/world/v0.0.1/welcome", "99999/newkotwo/v0.0.1/welcome", "99999/newko/v0.0.1/welcome" ]
  },
  "diskSpace" : {
    "status" : "UP",
    "total" : 499963170816,
    "free" : 418602975232,
    "threshold" : 10485760
  }    
```
The shelf is **UP** looking for KOs in the 
_/Users/developer/Downloads/activator/shelf/_ directory.  Activation service is *UP*, the service has 
loaded a _JAVASCRIPT_ adapter, the service found 3 KOs and activate three executor endpoints

## Validating the Activator 

With the aample shelf in place the following tests can be executed against the running activator

View a Knowledge Object

```
curl http://localhost:8080/hello/world
```

View a Knowledge Object Version

```
curl http://localhost:8080/hello/world/v0.0.1
```

Run the welcome endpoint on the hello/world/v0.0.1 knowledge object
```
curl -X POST -H "Content-Type:application/json"  -d "{\"name\": \"Fred Flintstone\"}" http://localhost:8080/hello/world/v0.0.1/welcome
```

## Configuration

### KO Shelf Location
By default the activator will look for a _shelf_ in jar execution directory but the location the _shelf_ can be configured:
```bash
java -jar kgrid-activator*.jar --kgrid.shelf.cdostore.filesystem.location=//data/myshelf
```

### Activator port 
To change the port:
```bash
java -jar kgrid-activator*.jar --server.port=9090
```

### Activator server path 
To change the the server root path
```bash
java -jar kgrid-activator*.jar --server.contextPath=/activator
```