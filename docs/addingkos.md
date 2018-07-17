---
layout: page
navtitle: Adding New Knowledge Objects

---

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