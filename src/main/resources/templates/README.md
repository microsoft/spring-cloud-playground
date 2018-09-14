# Getting Started

## Requirements
Install [Maven](https://maven.apache.org/install.html) and [Docker](https://docs.docker.com/install/).

## Configuration
Configuration Azure service module from the [spring-cloud-azure-samples](https://github.com/Microsoft/spring-cloud-azure/tree/master/spring-cloud-azure-samples).

## Run
#### Option 1: Docker-compose (Recommended)
```
cd docker && run.cmd (cd docker && run.sh)
```

#### Option 2: Local machine
```
mvn clean package
java -jar ${azure-cloud-module}/target/demo.${azure-cloud-module}-0.0.1-SNAPSHOT.jar
```
After build success, run the jar package of modules with the sequences in below:
* Config server
* Eureka server
* Hystrix dashboard if exist
* Any other Azure service module
* Gateway
  
