# KGrid Docker Containers


KGrid Activator can be deployed using docker container architecture.

## Get the docker image ready
The container image for the activator can be either built from the source code or pulled from DockerHub.

### Build from source code

Clone the [kgrid-activator repo](https://github.com/kgrid/kgrid-activator). 

In kgrid activator folder run:

   ```bash
   mvn spring-boot:build-image 
   ```

This will use the default builder from Spring-boot to build the image.

After this run you will have a local docker image...
```
~/kgrid-activator $ docker images
REPOSITORY                  TAG                   IMAGE ID            CREATED             SIZE
kgrid/activator             latest                fbe2de94cfa9        3 minutes ago       149MB
```

:::tip
You can also specify other builders by adding the option to the maven command; for example `-Dspring-boot.build-image.builder=heroku/spring-boot-buildpacks`.  
:::

### Pull from DockerHub
  ```bash
  docker pull kgrid/activator:#.#.#
  ```

## Using the Image

Now using the activator image you can create a container name ***activator*** from the `kgrid/activator` image and run it on port 8080...
 
```docker run -p 8080:8080 --name activator kgrid/activator```

or mapped to local shelf and running in the background...

```docker run -p 8080:8080 -v ${PWD}/shelf:/home/kgrid/shelf --name activator -d kgrid/activator ```

Once created, you can stop and start the container using `docker stop activator` and `docker start acivator`.

## Quick start with `docker-compose`

You can also start the activator in your environment by setting up `docker-compose.yaml` file, shown below as an example
```yaml
version: "3.6"

services:
  activator:
    container_name: lion-activator
    environment:
        KGRID_CONFIG: "--kgrid.shelf.cdostore.url=filesystem:file://shelf --cors.url=*  --management.info.git.mode=full"
    image: kgrid/activator:1.5.2
    ports:
      - 8080:8080
    volumes:
      - "activator_shelf:/home/kgrid/shelf"

volumes:
  activator_shelf:
```

that uses the the `kgrid/activator:1.5.2` image, with presets for port and shelf. 

Then:

```docker-compose up```



#### Good to Know

1. View Container Logs  ```docker logs activator```
1. Start a shell in the container ```docker exec -it activator sh```


### Push New Image

Activator images are stored on [DockerHub](https://cloud.docker.com/u/kgrid/repository/docker/kgrid/activator) 

```docker push kgrid/activator:#.#.# ```

