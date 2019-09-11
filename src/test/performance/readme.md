### using the loadimpact/k6 load testing tool

### Install k6
 
 https://docs.k6.io/docs for installation instructions
 
### Create a local shelf
Current the performance scripts use the [hello world](https://github.com/kgrid-objects/example-collection) KO and [opioid collection](https://github.com/kgrid-objects/opioid-collection).  

- Download [hello-world.zip](https://github.com/kgrid-objects/example-collection/releases/latest) and [99999-10103.zip ](https://github.com/kgrid-objects/opioid-collection/releases/latest)the sites listed above
- create a local shelf directory
- Place the hello-world.zip and 99999-10103.zip nto the shelf directory and unzip. This will place the KOs into the shelf directory.
 

### Start Activator using test KOs
Open a terminal window at the kgrid-activator project root 
```
java -jar target/kgrid-activator*.jar --kgrid.shelf.cdostore.url=filesystem:file:///data/myshelf

```

### K6 performance script examples
With the Activator running using the shelf KOs you can run these performance scripts.  Open Terminal session in the performance folder.

#####Simple 'list of KOs' test
(with 5 virtual users for 5 seconds)
```bash
k6 run k6-scripts/get_objects.js --vus 5 --duration 5s
```

#####Simple post hello world knowledge object

```bash
k6 run k6-scripts/post_hello-world.js --vus 5 --duration 3s
```

#####Simple post Opioid Respiratory Depression Risk Indicator  knowledge object

```bash
k6 run k6-scripts/post_triple-threat.js --vus 5 --duration 3s
```

#####Overriding the base url and endpoint

```bash
k6 run k6-scripts/post_hello-world.js --vus 5 --duration 3s --env baseUrl=https://kgrid-activator.herokuapp.com --env endpoint=/hello/world/v0.1.0/welcome
```

#####Test using a `config.json` file

```bash
k6 run k6-scripts/post_hello-world.js --config k6-scripts/config.json
```
