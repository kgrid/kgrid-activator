FROM openjdk:8-jdk-alpine

MAINTAINER KGrid Team "kgrid-developers@umich.edu"

ENV KGRID_CONFIG=""

RUN apk update

RUN addgroup -S kgrid && adduser -S kgrid -G kgrid
USER kgrid

WORKDIR /home/kgrid
ARG JAR_FILE

COPY target/${JAR_FILE} .
RUN mkdir shelf

EXPOSE 8080
CMD  java -jar kgrid-activator*.jar $KGRID_CONFIG