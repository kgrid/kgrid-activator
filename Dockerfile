# Use the following command to build the image:
# sudo docker build --build-arg jar_file=<PATH TO JAR> --build-arg manifest=<PATH TO MANIFEST> -t activator .

# Use the following command to run the image:
# sudo docker run --network host activator

FROM openjdk:11
MAINTAINER kgrid (kgrid-developers@umich.edu)

ARG jar_file
ARG manifest
ARG shelf_location
ARG shelf_endpoint
ARG port

ENV MANIFEST=$manifest
ENV SHELF_LOCATION=$shelf_location
ENV SHELF_ENDPOINT=$shelf_endpoint
ENV PORT=$port

COPY ${jar_file} app.jar
ENTRYPOINT java -jar /app.jar --port=$PORT --kgrid.shelf.manifest=$MANIFEST --kgrid.shelf.cdostore.url=filesystem:file://$SHELF_LOCATION --kgrid.shelf.endpoint=$SHELF_ENDPOINT
