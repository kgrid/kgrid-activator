

## Adding MOpen Opioid Knowledge Objects

Add new KOs to the existing shelf. Once in the shelf directory you will need to activate the new 
KO objects.  For example to add the MOpen Opiod KOs.

1. Download the released MOpen Opioid KOs self ([opioid_shelf.zip](https://github.com/kgrid-objects/opioid-collection/releases/latest)) from github [MOpen-Opioid Collection](https://github.com/kgrid/opioid-collection/)
1. Put the opioid_shelf.zip into the directory where the activator jar is located and unzip.  This will place the KOs into the shelf directory

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

Once unzipped into the shelf directory the activator should auto-load the new knowledge objects.

``` http://localhost:8080/health ```

You should receive a list of the Endpoints loaded similar to the following 

```json
[
    "hello/world/v0.0.1/welcome",
    "99999/10103/v0.0.1/tripleThreatDetector",
    "99999/10101/v0.0.1/opioidDetector",
    "99999/10102/v0.0.1/opioidbzdDetector"
]
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
            "99999/10103/v0.0.1/tripleThreatDetector",
            "99999/10101/v0.0.1/opioidDetector",
            "99999/10102/v0.0.1/opioidbzdDetector"
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