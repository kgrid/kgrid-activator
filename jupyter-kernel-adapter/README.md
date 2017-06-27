# Knowledge Grid - Jupyter Kernel Adapter

## Use

### Prerequisites

* Install jupyter - [Install Instructions](http://jupyter.org/install.html)
* Install jupyter kernel gateway - [Install Instructions](https://jupyter-kernel-gateway.readthedocs.io/en/latest/getting-started.html#using-conda)

Note: Installing jupyter on Windows may ask for download of a C++ compiler - [Install Instructions](https://www.microsoft.com/en-us/download/details.aspx?id=44266)

### Running the Kernel

1. Run the jupyter kernel using the following command in the command line
    ```
    jupyter kernelgateway
    ```
1. Use Postman to verify that the kernel is properly running

    ```
    GET /api HTTP/1.1
    Host: localhost:8888
    Content-Type: application/json
    ```

    This should yield the following response
    ```
    { "version": "5.0.0" }
    ```

    This can also be verified on the command line using curl
    ```
    curl -X GET \
      http://localhost:8888/api \
      -H 'content-type: application/json
    ```

1. Start a python kernel
    ```
    POST /api/kernels HTTP/1.1
    Host: localhost:8889
    Content-Type: application/json
    Postman-Token: cd63276d-addb-92b9-0ad7-2adfa0b783ff
    ```

    ```
    curl -X POST \
      http://localhost:8889/api/kernels \
      -H 'cache-control: no-cache' \
      -H 'content-type: application/json' \
      -H 'postman-token: 1e9cf2aa-0792-efda-931f-028e2d3ff4cd' \
      -d '{"name":"python"}'
    ```

Note: If you receive the following response on Linux when starting the kernel gateway
```
{ "message": "",
  "reason": "Not Found" }
```

then use the following command line command to configure it
```
KG_ALLOW_ORIGIN="*" jupyter kernelgateway
```
