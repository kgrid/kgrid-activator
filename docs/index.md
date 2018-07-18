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

Download the latest activator jar from GitHub [Latest Activator Release](https://github.com/kgrid/kgrid-activator/releases/latest).

1. Download [kgrid-activator-0.6.2.jar](https://github.com/kgrid/kgrid-activator/releases/latest)  
1. Download [hello-world-shelf.zip](https://github.com/kgrid/kgrid-activator/releases/latest) into the directory where the activator jar is located and unzip.  This will place the KOs into the shelf directory


Directory structure should look similar to the following

```     
 ├── shelf
 │   └── hello-world  
 │       └── v0.0.1
 │           ├── model
 │           └── metadata.json
 └── kgrid-activator-0.6.2.jar
```

The activator is executable jar and can be run from the command line.  Open a terminal window and navigate to the direcoty where the jar and shelf are located.  

Type in the following. 

```java -jar kgrid-activator-0.6.2.jar ```

By default the activator will run on port 8080. You can validate the activator is up and running using 
the activators health endpoint. In your browser type in the following URL address.

```http://localhost:8080/health```

The health of the Activator should display a status of **UP**.  Other informatoin about 
Knowledage Objects, Adapters and EndPoints can also be found.  Details about these attributes be
discussed later.

```
{
   status: "UP",
   shelf: {
      status: "UP",
      kgrid.shelf.cdostore.url: "shelf"
   },
   activationService: {
      status: "UP",
      Knowledge Objects found: 1,
      Adapters loaded: [
        "JAVASCRIPT",
        "PROXY"
       ],
   EndPoints loaded: [
        "hello/world/v0.0.1/welcome"
   ]
   },
   diskSpace: {
      status: "UP",
      total: 499963170816,
      free: 415911948288,
      threshold: 10485760
   }
 }
   ```

## Validating the Hello-World KO on the Activator 

With the sample shelf in place the following tests can be executed against the running activator

View a Knowledge Object

```curl http://localhost:8080/hello/world```

View a Knowledge Object Version

```curl http://localhost:8080/hello/world/v0.0.1```

Run the welcome endpoint on the hello/world/v0.0.1 knowledge object

```curl -X POST -H "Content-Type:application/json"  -d "{\"name\": \"Fred Flintstone\"}" http://localhost:8080/hello/world/v0.0.1/welcome```

The Hello World KO will return the following

```aidl
{
    "result": "Welcome to Knowledge Grid, Fred Flintstone",
    "info": {
        "ko": "hello/world/v0.0.1",
        "inputs": {
            "name": "Fred Flintstone"
        }
    }
}
```

