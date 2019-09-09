## KGrid Docker Container

### Build Image

To build a local docker image...

We are using [Spotify's Dockerfile Maven plug](https://github.com/spotify/dockerfile-maven) to create a docker image of the current activator project (the default is to build `kgrid/activator:latest`).  

``` mvn clean package dockerfile:build```

Or to build a tagged image (try to match the git tag)...

``` mvn clean package dockerfile:build  -Ddockerfile.tag=1.0.4-rc2```

After this run you will have a local docker image...
```
~/kgrid-activator $ docker images
REPOSITORY                  TAG                   IMAGE ID            CREATED             SIZE
kgrid/activator             latest                fbe2de94cfa9        3 minutes ago       149MB
```
