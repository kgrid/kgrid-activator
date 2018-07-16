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

1. Download _kgrid-activator-0.6.2.jar_  
1. Download _hello-world-shelf.zip_ into the directory where the activator jar is located and unzip.  This will place the KOs into the shelf directory


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

By default the activator will run on port 8080. You can validate the activator is up and running using the activators health endpoint. All _statuses_ reported should be **UP**

```curl http://localhost:8080/health```

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

## Configuration

**Activator Knowledge Object Shelf Location**

By default the activator will look for a _shelf_ in jar execution directory but the location the _shelf_ can be configured:

```bash
java -jar kgrid-activator-0.6.2.jar --kgrid.shelf.cdostore.url=filesystem:file:///data/myshelf
```

**Activator Server Port** 

To change the port:

```java -jar kgrid-activator-0.6.2.jar --server.port=9090```

Now access health

```curl http://localhost:9090/health```


**Activator Server Path** 

By default the endpoints of the activator at the root of the activator server.  To change the server root path:

```java -jar kgrid-activator-0.6.2.jar --server.contextPath=/activator```

Now access health

```curl http://localhost:8080/activator/health```


## Adding New Knowledge Objects

Add new KOs to the existing shelf. Once in the shelf directory you will need to activate the new 
KO objects.  For example to add the MOpen Opiod KOs.

1. Download the released MOpen Opiod KOs self (_opid_shelf.zip_) from github [MOpen-Opioid Collection](https://github.com/kgrid/mopen-opioid-collection/releases/latest)
1. Download _opid_shelf.zip.zip_ into the directory where the activator jar is located and unzip.  This will place the KOs into the shelf directory

Directory structure should look similar to the following

```     
 ├── shelf
 │   └── hello-world  
 │       └── v0.0.1 
 │   └── 99999-10103
 │       └── v0.0.1   
 │   └── 99999-10102
 │       └── v0.0.1   
 │   └── 99999-10101
 │       └── v0.0.1   
 └── kgrid-activator-0.6.2.jar
```

Once loaded into the shelf directory the KOs will need to be activated.  This is accomplished by calling the executors resource.  

```curl http://localhost:8080/endpoints```

This will load and activate the KOs on the shelf.  You should recieve a list of the activated endpoint similar to the following 

```json
[
    "hello/world/v0.0.1/welcome",
    "99999/10103/v0.0.1/tripleThreatDetector",
    "99999/10101/v0.0.1/opioidDetector",
    "99999/10102/v0.0.1/opioidbzdDetector"
]
```
