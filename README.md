Steps to install python execution stack
1. Clone GIT repository - 'git@git.umms.med.umich.edu:LHS/python-execution-stack.git'
   - Command to build project mvn clean install
   - Jar file will be created under target folder

2. Python Execution stack configuration expects following parameters to be configured
   - library.absolutePath = ObjectTeller library path <e.g. http://dlhs-fedora-dev-a.umms.med.umich.edu:8080/ObjectTeller>
        - By default, execution stack would search for object teller library in same the same context path . e.g. If execution stack is deployed at http://localhost:8080/ 
        then python execution stack would look for objectTeller library at http://localhost:8080/
   - library.username = ObjectTeller library API Key or username assigned to you (Not implemented yet. ) (default value - )
   - library.password = ObjectTeller password (Not implemented yet. ) (default value - )
   - executionStack.localStoragePath = By default it will be temp folder of the operating system given by java.io.tmpdir property. You can customize this property. 
   
   All the configurations can overwritten by supplying your own configaration file while installing python execution stack. 

3. Run Jar File 
    By default tomcat would be deployed at port : 8080
    Attribute to supply configuration file: spring.config.location 'spring.config.location=file:/path/to/cong/' by default it will load configuration from application.properties
    or you can supply different application properties file using 'spring.config.location=file:/path/to/cong/my-application.properties'

    Command install python execution stack
    java -jar target/<jar-file-name>.jar -spring.config.location=file:/path/to/cong/
    or
    java -jar target/<jar-file-name>.jar -spring.config.location=file:/path/to/cong/my-application.properties

 


