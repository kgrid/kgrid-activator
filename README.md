# Knowledge Grid — Python Execution Stack

## Installing

    $ git clone https://github.com/kgrid/python-execution-stack.git

    $ cd python-execution-stack
First, build and deploy the execution stack as a standard jar file:
    $ mvn clean package

    $ java -Dpython.import.site=false -jar target/python-execution-stack.jar

(the `-Dpython.import.site=false` is required when running as a bare jar file. It is not required when the war file is deployed in a container.)


the following properties can be configured on the caommand line using '`-Doption=value`':


    -Dlibrary.absolutePath=http://kgrid.umich.edu/ObjectTeller    # Default is no external library

    -Dstack.localStoragePath=/Users/kgrid/ko/    # Default is system property 'java.io.tmpdir'

    -Dstack.lshelName=mycoolshelf    # Default is 'shelf' in the local storage path identified above

    -Dserver.port=8082    # Default is '8080'

In all cases the property can be specified via a system property as well

    export SERVER_PORT=8083

See [Spring Boot external property configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html) for more detail.


It is also possible to supply the location of an external application properties file:

    -Dspring.config.location=file:/path/to/config/folder'

 by default it will load configuration from application.properties
    or you can supply different application properties file using

    spring.config.location=file:/path/to/config/folder/my-application.properties


## Build and installing in a container (Tomcat, WAR file)

    $ mvn clean package -f pom-war.xml

the war file will be in `target`

## Loading test knowledge object and testing the execution stack


 


