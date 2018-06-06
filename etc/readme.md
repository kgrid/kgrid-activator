### using the loadimpact/k6 load testing tool

### Install k6
 
 https://docs.k6.io/docs for installation instructions

### Start Activator using test KOs
Open a terminal window at the kgrid-activator project root 
```
java -jar target/kgrid-activator*.jar --kgrid.shelf.cdostore.filesystem.location=etc/shelf
```

### K6 performance script examples
With the Activator running using the etc/shelf KOs you can run these performance scripts

#####Simple get KOs test
```bash
cd etc
k6 run k6-scripts/get_objects.js
```

#####Simple post hello world knowledge object

```bash
k6 run k6-scripts/post_hello-world.js --vus 5 --duration 3s
```

#####Overriding the base url and endpoint

```bash
k6 run k6-scripts/post_hello-world.js --vus 5 --duration 3s --env baseUrl=https://kgrid-activator.herokuapp.com --env endpoint=/99999/newko/v0.0.1/welcome
```

#####Test using a `config.json` file

```bash
k6 run k6-scripts/post_hello-world.js --config k6-scripts/config.json
```