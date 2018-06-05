### using the loadimpact/k6 load testing tool

See https://docs.k6.io/docs for installation instructions

```bash
k6 run k6-scripts/get_objects.js
```

for a simple post hello world knowledge object

```bash
k6 run k6-scripts/post_hello-world.js --vus 5 --duration 3s
```

or overriding the base url and endpoint

```bash
k6 run k6-scripts/post_hello-world.js --vus 5 --duration 3s --env baseUrl=https://kgrid-activator.herokuapp.com --env endpoint=/99999/newko/v0.0.1/welcome
```

or using a `config.json` file

```bash
k6 run k6-scripts/post_hello-world.js --config k6-scripts/config.json
```